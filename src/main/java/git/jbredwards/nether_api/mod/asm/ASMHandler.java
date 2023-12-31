/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                // Modded
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBetterNetherConfigLoader", // Prevent BetterNether from resetting its enabled biomes config cache
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBetterNetherFirefly", // Fix BetterNether firefly spawning code
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBetterNetherGenerator", // Allow BetterNether to use real biomes instead of pseudo-biomes
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBiomesOPlentyBiomes", // Change Biomes O' Plenty's nether biome super classes
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBiomesOPlentyDecorator", // Allow BOP nether features to always generate in the nether
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerBiomesOPlentyFixes", // Disable Biomes O' Plenty's fog override in dimensions with their own fog handlers
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerJITLCascadingFix", // Fix cascading world gen problems with Journey Into The Light
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerJITLGenerator", // Allow Journey Into The Light to use real biomes instead of pseudo-biomes
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerJITLTowerFix", // Fix Journey Into The Light's WorldGenNetherTower using bad registry names for spawners
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerLibraryExCascadingFix", // Fix cascading world gen problems with LibraryEx
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerNethercraftEvents", // Ensure all Nethercraft world generation is kept to within one biome
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerNetherEXBiomes", // Change NetherEx's nether biome super classes
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerNetherExOverride", // Disable NetherEx's nether override
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerStygianEndBiomes", // Change Stygian End's biome super classes
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerStygianEndCascadingFix", // Fix cascading world gen problems with Stygian End
                // Modded Nether Height (changes various modded nether generators to not use hardcoded height values)
                "git.jbredwards.nether_api.mod.asm.transformers.modded.height.Transformer_NetherHeight_BiomesOPlenty",
                "git.jbredwards.nether_api.mod.asm.transformers.modded.height.Transformer_NetherHeight_Natura",
                "git.jbredwards.nether_api.mod.asm.transformers.modded.height.Transformer_NetherHeight_NetherEx",
                // Vanilla
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.Transformer_MC_10369", // Fixes vanilla attempting to spawn particles from the server using a client-side method
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBiomeEndDecorator", // Use seed-coord-based random and un-hardcode obsidian spike generation
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBiomeHell", // All BiomeHell instances use netherrack as their top and filler blocks
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerCommandLocate", // Add registered structures to the /locate tab completion list
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerDragonSpawnManager", // Use seed-coord-based random and un-hardcode obsidian spike regeneration
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerEntityRenderer", // Fix MC-31681 (Fog and clouds darken when indoors or under trees)
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerMapGenCavesHell", // Ensures that nether caves can carve through any biome
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerMapGenEndCity", // Allow IEndBiome instances to specify whether they're an applicable biome
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerWorldClient" // Handle biome ambient sounds and particles from this mod's end
        };
    }

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
