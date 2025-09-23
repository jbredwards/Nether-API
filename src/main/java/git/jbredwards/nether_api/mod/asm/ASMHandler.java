/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Streams;
import git.jbredwards.nether_api.mod.asm.transformers.modded.betternether.TransformerBetterNetherConfigLoader;
import git.jbredwards.nether_api.mod.asm.transformers.modded.betternether.TransformerBetterNetherFirefly;
import git.jbredwards.nether_api.mod.asm.transformers.modded.betternether.TransformerBetterNetherGenerator;
import git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty.*;
import git.jbredwards.nether_api.mod.asm.transformers.modded.jitl.TransformerJITLCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.jitl.TransformerJITLGenerator;
import git.jbredwards.nether_api.mod.asm.transformers.modded.jitl.TransformerJITLTowerFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.libraryex.TransformerLibraryExCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.natura.Transformer_NetherHeight_Natura;
import git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft.TransformerNethercraftEvents;
import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.TransformerNetherEXBiomes;
import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.TransformerNetherExOverride;
import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.Transformer_NetherHeight_NetherEx;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndBiomes;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("Nether API Plugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public final class ASMHandler implements IFMLLoadingPlugin
{
    @SuppressWarnings({"UnstableApiUsage", "unused"})
    public static final class Transformer implements IClassTransformer
    {
        @Nonnull public final ListMultimap<String, IClassTransformer> transformers = MultimapBuilder.hashKeys().arrayListValues().build();
        @Nonnull public final List<IClassTransformer> globalTransformers = new ArrayList<>();

        public Transformer() {
            // BetterNether:
            {
                // Prevent BetterNether from resetting its enabled biomes config cache.
                register(new TransformerBetterNetherConfigLoader(),
                        "paulevs.betternether.config.ConfigLoader");
                // Fix BetterNether firefly spawning code.
                register(new TransformerBetterNetherFirefly(),
                        "paulevs.betternether.entities.EntityFirefly");
                // Allow BetterNether to use real biomes instead of pseudo-biomes.
                register(new TransformerBetterNetherGenerator());
            }
            // Biomes O' Plenty:
            {
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_BiomesOPlenty(),
                        "biomesoplenty.common.biome.nether.BOPHellBiome",
                        "biomesoplenty.common.util.biome.GeneratorUtils$ScatterYMethod",
                        "biomesoplenty.common.util.block.BlockQuery$BlockPosQueryAltitude");
                // Change Biomes O' Plenty's nether biome super classes.
                register(new TransformerBiomesOPlentyBiomes());
                // Allow BOP nether features to always generate in the nether.
                register(new TransformerBiomesOPlentyDecorator(),
                        "biomesoplenty.common.handler.decoration.DecorateBiomeEventHandler");
                // Disable Biomes O' Plenty's fog override in dimensions with their own fog handlers.
                register(new TransformerBiomesOPlentyFixes(),
                        "biomesoplenty.common.handler.FogEventHandler",
                        "biomesoplenty.common.world.generator.GeneratorBramble",
                        "biomesoplenty.common.world.generator.GeneratorHive",
                        "zmaster587.advancedRocketry.event.PlanetEventHandler");
                // Prevent certain BOP fluids from vaporizing.
                register(new TransformerBiomesOPlentyFluids(),
                        "biomesoplenty.common.fluids.BloodFluid",
                        "biomesoplenty.common.fluids.HoneyFluid");
            }
            // Journey Into The Light:
            {
                // Fix cascading world gen problems with Journey Into The Light.
                register(new TransformerJITLCascadingFix(),
                        "net.journey.dimension.base.WorldGenJourney",
                        "net.slayer.api.worldgen.WorldGenAPI");
                // Allow Journey Into The Light to use real biomes instead of pseudo-biomes.
                register(new TransformerJITLGenerator(),
                        "net.journey.eventhandler.NetherEvent");
                // Fix Journey Into The Light's WorldGenNetherTower using bad registry names for spawners.
                register(new TransformerJITLTowerFix(),
                        "net.journey.dimension.nether.gen.WorldGenNetherTower");
            }
            // LibraryEx:
            {
                // Fix cascading world gen problems with LibraryEx.
                register(new TransformerLibraryExCascadingFix(),
                        "logictechcorp.libraryex.utility.StructureHelper");
            }
            // Natura:
            {
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_Natura(),
                        "com.progwml6.natura.world.worldgen.berry.nether.NetherBerryBushGenerator",
                        "com.progwml6.natura.world.worldgen.trees.nether.BloodwoodTreeGenerator",
                        "com.progwml6.natura.world.worldgen.GlowshroomGenerator",
                        "com.progwml6.natura.world.worldgen.NetherMinableGenerator",
                        "com.progwml6.natura.world.worldgen.NetherTreesGenerator",
                        "com.progwml6.natura.world.worldgen.VineGenerator",
                        "maxhyper.dynamictreesnatura.worldgen.BiomeDataBasePopulator");
            }
            // Nethercraft Classic:
            {
                // Ensure all Nethercraft world generation is kept to within one biome.
                register(new TransformerNethercraftEvents(),
                        "com.legacy.nethercraft.entities.hostile.EntityBloodyZombie",
                        "com.legacy.nethercraft.entities.hostile.EntityDarkZombie",
                        "com.legacy.nethercraft.entities.hostile.EntityLavaSlime",
                        "com.legacy.nethercraft.entities.projectile.EntitySlimeEggs",
                        "com.legacy.nethercraft.entities.NetherEntityRegistry",
                        "com.legacy.nethercraft.world.NetherGenTree",
                        "com.legacy.nethercraft.world.NetherWorldEvent");
            }
            // NetherEx:
            {
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_NetherEx(),
                        "logictechcorp.netherex.handler.BiomeTraitGenerationHandler",
                        "logictechcorp.netherex.world.biome.data.BiomeDataNetherEx");
                // Change NetherEx's nether biome super classes.
                register(new TransformerNetherEXBiomes());
                // Disable NetherEx's nether override.
                register(new TransformerNetherExOverride(),
                        "logictechcorp.netherex.handler.BiomeTraitGenerationHandler",
                        "logictechcorp.netherex.world.biome.data.BiomeDataManagerNetherEx",
                        "logictechcorp.netherex.NetherEx");
            }
            // Stygian End:
            {
                // Change Stygian End's biome super classes.
                register(new TransformerStygianEndBiomes());
                // Fix cascading world gen problems with Stygian End.
                register(new TransformerStygianEndCascadingFix(),
                        "fluke.stygian.world.feature.WorldGenEnderCanopy",
                        "fluke.stygian.world.feature.WorldGenEndVolcano");
            }
            // Vanilla:
            {
                // Fixes vanilla attempting to spawn particles from the server using a client-side method.
                register(new Transformer_MC_10369(),
                        "com.therandomlabs.randompatches.patch.ServerWorldEventHandlerPatch",
                        "net.minecraft.block.BlockLiquid",
                        "net.minecraft.block.BlockPumpkin",
                        "net.minecraft.block.BlockRedstoneTorch",
                        "net.minecraft.block.BlockSkull",
                        "net.minecraft.entity.ai.EntityAIMate",
                        "net.minecraft.entity.boss.EntityDragon",
                        "net.minecraft.entity.EntityLivingBase",
                        "net.minecraft.item.ItemEnderEye");
                // Use seed-coord-based random and un-hardcode obsidian spike generation.
                register(new TransformerBiomeEndDecorator(),
                        "net.minecraft.world.biome.BiomeEndDecorator");
                // All BiomeHell instances use netherrack as their top and filler blocks.
                register(new TransformerBiomeHell(),
                        "net.minecraft.world.biome.BiomeHell");
                // Add registered structures to the /locate tab completion list.
                register(new TransformerCommandLocate(),
                        "net.minecraft.command.CommandLocate");
                // Use seed-coord-based random and un-hardcode obsidian spike regeneration.
                register(new TransformerDragonSpawnManager(),
                        "net.minecraft.world.end.DragonSpawnManager$3");
                // Fix MC-31681 (Fog and clouds darken when indoors or under trees).
                register(new TransformerEntityRenderer(),
                        "net.minecraft.client.renderer.EntityRenderer");
                // Ensures that nether caves can carve through any biome.
                register(new TransformerMapGenCavesHell(),
                        "net.minecraft.world.gen.MapGenCavesHell");
                // Allow IEndBiome instances to specify whether they're an applicable biome.
                register(new TransformerMapGenEndCity(),
                        "net.minecraft.world.gen.structure.MapGenEndCity");
                // Handle biome ambient sounds and particles from this mod's end.
                register(new TransformerWorldClient(),
                        "net.minecraft.client.multiplayer.WorldClient");
            }
        }

        private void register(@Nonnull final IClassTransformer transformer, @Nonnull final String... filter) {
            if(filter.length != 0) for(@Nonnull final String key : filter) transformers.put(key, transformer);
            else globalTransformers.add(transformer);
        }

        @Nullable
        @Override
        public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
            return basicClass == null || transformedName == null ? basicClass : Streams
                    .concat(transformers.get(transformedName).stream(), globalTransformers.stream())
                    .reduce(basicClass, (bc, ct) -> ct.transform(name, transformedName, bc), (bc1, bc2) -> bc2);
        }
    }

    @Nonnull
    @Override
    public String[] getASMTransformerClass() { return new String[] { getClass().getName() + "$Transformer"}; }

    // ==============
    // NOT APPLICABLE
    // ==============

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(@Nonnull Map<String, Object> data) {}

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
