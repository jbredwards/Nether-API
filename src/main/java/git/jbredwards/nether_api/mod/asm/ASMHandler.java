/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.mod.asm;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Streams;
import git.jbredwards.nether_api.mod.asm.transformers.modded.betternether.*;
import git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty.*;
import git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround.TransformerBiomeFogColorCalculator;
import git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround.TransformerFogHandler;
import git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround.TransformerStormRenderer;
import git.jbredwards.nether_api.mod.asm.transformers.modded.dynamictrees.TransformerSpeciesHellbark;
import git.jbredwards.nether_api.mod.asm.transformers.modded.dynamictrees.Transformer_NetherHeight_DynamicTrees;
import git.jbredwards.nether_api.mod.asm.transformers.modded.hexed.TransformerNetherHexedKingdom;
import git.jbredwards.nether_api.mod.asm.transformers.modded.jitl.*;
import git.jbredwards.nether_api.mod.asm.transformers.modded.justenoughdimensions.TransformerDimensionTypeEntry;
import git.jbredwards.nether_api.mod.asm.transformers.modded.justenoughdimensions.TransformerWorldInfoUtils;
import git.jbredwards.nether_api.mod.asm.transformers.modded.libraryex.TransformerLibraryExCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.natura.TransformerBlockNetherSapling;
import git.jbredwards.nether_api.mod.asm.transformers.modded.natura.TransformerBlockTaintedSoil;
import git.jbredwards.nether_api.mod.asm.transformers.modded.natura.Transformer_NetherHeight_Natura;
import git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft.TransformerBlockGlowWoodSapling;
import git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft.TransformerBlockNetherDirt;
import git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft.TransformerBlockNetherReed;
import git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft.TransformerNethercraftEvents;
import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.*;
import git.jbredwards.nether_api.mod.asm.transformers.modded.perfectspawn.TransformerAsmHandler;
import git.jbredwards.nether_api.mod.asm.transformers.modded.quark.TransformerQuarkCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndBiomes;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndCascadingFix;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndGrass;
import git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end.TransformerStygianEndPlant;
import git.jbredwards.nether_api.mod.asm.transformers.modded.voidislandcontrol.TransformerEventHandler;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1002)
@IFMLLoadingPlugin.Name("Nether API Plugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public final class ASMHandler implements IFMLLoadingPlugin
{
    @Override
    public void injectData(@Nonnull final Map<String, Object> data) {
        // Ensure ITransformer is fully loaded before transforms start.
        try(@Nonnull final JarFile jar = new JarFile((File)data.get("coremodLocation"))) {
            for(@Nonnull final Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements();) {
                @Nonnull final JarEntry entry = it.nextElement();
                if(!entry.isDirectory() && entry.getName().startsWith("git/jbredwards/nether_api/mod/asm/transformers/ITransformer$")) {
                    Class.forName(entry.getName().replace('/', '.').substring(0, entry.getName().length() - ".class".length()));
                }
            }
        }

        // Unpossible?
        catch(@Nonnull final IOException | ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    @SuppressWarnings({"UnstableApiUsage", "unused"})
    public static final class Transformer implements IClassTransformer
    {
        @Nonnull public final ListMultimap<String, IClassTransformer> transformers = MultimapBuilder.hashKeys().arrayListValues().build();
        @Nonnull public final List<IClassTransformer> globalTransformers = new ArrayList<>();

        public Transformer() {
            // BetterNether:
            {
                // Run world load event before generation.
                register(new TransformerEventsHandler(),
                        "paulevs.betternether.events.EventsHandler");
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_BetterNether(),
                        "paulevs.betternether.structures.plants.StructureEye",
                        "paulevs.betternether.structures.plants.StructureStalagnate");
                // Prevent BetterNether from resetting its enabled biomes config cache.
                register(new TransformerBetterNetherConfigLoader(),
                        "paulevs.betternether.config.ConfigLoader");
                // Fix BetterNether firefly spawning code.
                register(new TransformerBetterNetherFirefly(),
                        "paulevs.betternether.entities.EntityFirefly");
                // Allow BetterNether to use real biomes instead of pseudo-biomes.
                register(new TransformerBetterNetherGenerator());
                // Allow BetterNether plants to recognize modded soil blocks.
                register(new TransformerBetterNetherPlants(),
                        "paulevs.betternether.blocks.BlockBlackApple",
                        "paulevs.betternether.blocks.BlockBlackAppleSeed",
                        "paulevs.betternether.blocks.BlockBlackBush",
                        "paulevs.betternether.blocks.BlockEggPlant",
                        "paulevs.betternether.blocks.BlockEyeSeed",
                        "paulevs.betternether.blocks.BlockEyeVine",
                        "paulevs.betternether.blocks.BlockInkBush",
                        "paulevs.betternether.blocks.BlockInkBushSeed",
                        "paulevs.betternether.blocks.BlockLucisSpore",
                        "paulevs.betternether.blocks.BlockMold",
                        "paulevs.betternether.blocks.BlockNetherGrass",
                        "paulevs.betternether.blocks.BlockNetherReed",
                        "paulevs.betternether.blocks.BlockOrangeMushroom",
                        "paulevs.betternether.blocks.BlockStalagnateSeed",
                        "paulevs.betternether.blocks.BlockStalagnateSeedBottom",
                        "paulevs.betternether.blocks.BlockWartSeed");
                // Add support for new EnumPlantTypes to BetterNether soil blocks.
                register(new TransformerBlockNetherMycelium(),
                        "paulevs.betternether.blocks.BlockNetherMycelium",
                        "paulevs.betternether.blocks.BlockNetherrackMoss");
                // Don't let BetterNether forks place black apple plants in the air.
                register(new TransformerStructureBlackApple(),
                        "paulevs.betternether.structures.plants.StructureBlackApple");
                // Don't let eye vines carve through terrain.
                register(new TransformerStructureEye(),
                        "paulevs.betternether.structures.plants.StructureEye");
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
                // Support modded "Netherrack"-like soil blocks.
                register(new TransformerBlockBOPFlower(),
                        "biomesoplenty.common.block.BlockBOPFlower");
                // Add support for new EnumPlantTypes to BOP soil blocks.
                register(new TransformerBlockBOPGrass(),
                        "biomesoplenty.common.block.BlockBOPGrass");
                // Support modded "Netherrack"-like soil blocks.
                register(new TransformerBlockBOPSapling(),
                        "biomesoplenty.common.block.BlockBOPSapling");
            }
            // Dynamic Surroundings:
            {
                // Support Nether API fog color events.
                register(new TransformerBiomeFogColorCalculator(),
                        "org.orecruncher.dsurround.client.handlers.fog.BiomeFogColorCalculator");
                // Account for world.provider.doesXZShowFog().
                register(new TransformerFogHandler(),
                        "org.orecruncher.dsurround.client.handlers.FogHandler");
                // Support Nether API fog color events.
                register(new TransformerStormRenderer(),
                        "org.orecruncher.dsurround.client.renderer.weather.StormRenderer");
            }
            // Dynamic Trees:
            {
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_DynamicTrees(),
                        "com.ferreusveritas.dynamictrees.worldgen.WorldGeneratorTrees$GroundFinder");
                // Fix some bugs with the BOP hellback dynamic trees species.
                register(new TransformerSpeciesHellbark(),
                        "dynamictreesbop.trees.TreeHellbark$SpeciesHellbark");
            }
            // Journey Into The Light:
            {
                // Add support for new EnumPlantTypes to Journey Into The Light soil blocks.
                register(new TransformerBlockMod(),
                        "net.slayer.api.block.BlockMod");
                // Allow nether saplings to be planted on the blocks they generate on.
                register(new TransformerBlockModSapling(),
                        "net.journey.blocks.base.BlockModSapling");
                // Support modded "Netherrack"-like and "Soul Sand"-like soil blocks
                register(new TransformerGroundPredicate(),
                        "net.journey.api.block.GroundPredicate");
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
            // Just Enough Dimensions:
            {
                // Ensure compatible Nether API world providers are used instead of old ones.
                register(new TransformerDimensionTypeEntry(),
                        "fi.dy.masa.justenoughdimensions.config.DimensionTypeEntry");
                // Account for modded DerivedWorldInfo instances.
                register(new TransformerWorldInfoUtils(),
                        "fi.dy.masa.justenoughdimensions.util.world.WorldInfoUtils");
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
                // Support modded "Netherrack" blocks.
                register(new TransformerBlockNetherSapling(),
                        "com.progwml6.natura.nether.block.saplings.BlockNetherSapling",
                        "com.progwml6.natura.nether.block.saplings.BlockNetherSapling2");
                // Add support for new EnumPlantType to tainted soil.
                register(new TransformerBlockTaintedSoil(),
                        "com.progwml6.natura.nether.block.soil.BlockTaintedSoil");
            }
            // Nether Hexed Kingdom:
            {
                // Allow structures to work with increased nether height, and fix some cascading world gen issues.
                register(new TransformerNetherHexedKingdom());
            }
            // Nethercraft Classic:
            {
                // Support modded "Netherrack"-like soil blocks.
                register(new TransformerBlockGlowWoodSapling(),
                        "com.legacy.nethercraft.blocks.natural.BlockGlowWoodSapling",
                        "com.legacy.nethercraft.blocks.natural.BlockNetherMushroom");
                // Add support for new EnumPlantTypes to Nethercraft's soil blocks.
                register(new TransformerBlockNetherDirt(),
                        "com.legacy.nethercraft.blocks.natural.BlockNetherDirt",
                        "com.legacy.nethercraft.blocks.natural.BlockNetherFarmland");
                // Support modded "Netherrack"-like and "Soul Sand"-like soil blocks, as well as Heat Sand.
                register(new TransformerBlockNetherReed(),
                        "com.legacy.nethercraft.blocks.natural.BlockNetherReed");
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
                // Support modded "Soul Sand"-like soil blocks.
                register(new TransformerBlockThornstalk(),
                        "logictechcorp.netherex.block.BlockThornstalk");
                // Change nether generators to not use hardcoded height values.
                register(new Transformer_NetherHeight_NetherEx(),
                        "logictechcorp.netherex.handler.BiomeTraitGenerationHandler",
                        "logictechcorp.netherex.world.biome.data.BiomeDataNetherEx");
                // Support the conversion of certain modded netherrack blocks.
                register(new TransformerInputHandler(),
                        "logictechcorp.netherex.handler.InputHandler");
                // Change NetherEx's nether biome super classes.
                register(new TransformerNetherEXBiomes());
                // Disable NetherEx's nether override.
                register(new TransformerNetherExOverride(),
                        "logictechcorp.netherex.handler.BiomeTraitGenerationHandler",
                        "logictechcorp.netherex.handler.WorldHandler",
                        "logictechcorp.netherex.world.biome.data.BiomeDataManagerNetherEx",
                        "logictechcorp.netherex.NetherEx");
            }
            // Perfect Spawn:
            {
                // Use the fallback value.
                register(new TransformerAsmHandler(),
                        "lumien.perfectspawn.handler.AsmHandler");
            }
            // Quark:
            {
                // Fix Quark nether fossil cascading world gen.
                register(new TransformerQuarkCascadingFix(),
                        "vazkii.quark.world.world.NetherFossilGenerator");
            }
            // Stygian End:
            {
                // Change Stygian End's biome super classes.
                register(new TransformerStygianEndBiomes());
                // Fix cascading world gen problems with Stygian End.
                register(new TransformerStygianEndCascadingFix(),
                        "fluke.stygian.world.feature.WorldGenEnderCanopy",
                        "fluke.stygian.world.feature.WorldGenEndVolcano");
                // Add support for new EnumPlantTypes to Stygian End's soil block.
                register(new TransformerStygianEndGrass(),
                        "fluke.stygian.block.BlockEndGrass");
                // Support modded "End Stone"-like soil blocks
                register(new TransformerStygianEndPlant(),
                        "fluke.stygian.block.BlockEndGlowPlant",
                        "fluke.stygian.block.BlockEndCanopySapling",
                        "fluke.stygian.block.BlockEndTallGrass");
            }
            // Void Island Control:
            {
                // Don't crash the event handler if the player spawns in another dimension.
                register(new TransformerEventHandler(),
                        "com.bartz24.voidislandcontrol.EventHandler");
            }
            // Vanilla:
            {
                // Fixes vanilla attempting to spawn particles from the server using a client-side method.
                register(new Transformer_MC_10369(),
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
                // Add support for new EnumPlantTypes to Vanilla soil blocks.
                register(new TransformerBlock(),
                        "net.minecraft.block.Block");
                // Support modded "End Stone"-like soil blocks.
                register(new TransformerBlockChorusFlower(),
                        "net.minecraft.block.BlockChorusFlower");
                // Support modded "End Stone"-like soil blocks.
                register(new TransformerBlockChorusPlant(),
                        "net.minecraft.block.BlockChorusPlant");
                // Allow BetterNether's 3d mushroom feature to have more render options.
                register(new TransformerBlockMushroom(),
                        "net.minecraft.block.BlockMushroom");
                // Allow shrubs to be used in Nether world generation.
                register(new TransformerBlockTallGrass(),
                        "net.minecraft.block.BlockTallGrass");
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
                // Allow more than just the Overworld to be set as an initial spawn dimension.
                register(new TransformerNetHandlerPlayClient(),
                        "net.minecraft.client.network.NetHandlerPlayClient",
                        "net.minecraft.entity.player.EntityPlayer",
                        "net.minecraft.server.management.PlayerList",
                        "net.minecraft.server.MinecraftServer",
                        "net.minecraftforge.fml.common.network.handshake.NetworkDispatcher");
                // Allow any dimension to permit respawns.
                register(new TransformerPlayerChunkMap(),
                        "net.minecraft.server.management.PlayerChunkMap",
                        "net.minecraft.server.management.PlayerList",
                        "net.minecraft.world.gen.ChunkProviderServer",
                        "net.minecraft.world.WorldServer");
                // Allow for custom obsidian platform generation.
                register(new TransformerTeleporter(),
                        "net.minecraft.world.Teleporter");
                // Handle biome ambient sounds and particles from this mod's end.
                register(new TransformerWorldClient(),
                        "net.minecraft.client.multiplayer.WorldClient");
                // Fix player spawn locations in other dimensions.
                register(new TransformerWorldProvider(),
                        "net.minecraft.world.WorldProvider",
                        "net.minecraft.world.WorldProviderEnd",
                        "net.minecraft.world.WorldProviderHell");
                // Don't let other mods introduce particle bugs.
                register(new TransformerWorldServer(),
                        "net.minecraft.world.WorldServer",
                        "net.minecraft.world.WorldServerMulti");
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

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
