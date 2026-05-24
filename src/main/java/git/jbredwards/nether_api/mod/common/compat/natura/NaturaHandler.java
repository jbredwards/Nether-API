/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.common.compat.natura;

import git.jbredwards.nether_api.mod.asm.transformers.modded.natura.Transformer_NetherHeight_Natura;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Exists to help bridge the gap for new Natura forks that are no longer compatible with Nether API transformers.
 * @author jbred
 *
 */
public final class NaturaHandler
{
    @Nonnull
    public static Set<Biome> getValidBerryBiomes() { return Transformer_NetherHeight_Natura.Hooks.VALID_BERRY_BIOMES; }
    public static boolean isValidBerryBiome(@Nonnull final Biome biome) {
        return Transformer_NetherHeight_Natura.Hooks.canGenerate(getValidBerryBiomes(), biome);
    }

    @Nonnull
    public static Set<Biome> getValidGlowshroomBiomes() { return Transformer_NetherHeight_Natura.Hooks.VALID_GLOWSHROOM_BIOMES; }
    public static boolean isValidGlowshroomBiome(@Nonnull final Biome biome) {
        return Transformer_NetherHeight_Natura.Hooks.canGenerate(getValidGlowshroomBiomes(), biome);
    }

    @Nonnull
    public static Set<Biome> getValidMinableBiomes() { return Transformer_NetherHeight_Natura.Hooks.VALID_MINABLE_BIOMES; }
    public static boolean isValidMinableBiome(@Nonnull final Biome biome) {
        return Transformer_NetherHeight_Natura.Hooks.canGenerate(getValidMinableBiomes(), biome);
    }

    @Nonnull
    public static Set<Biome> getValidTreeBiomes() { return Transformer_NetherHeight_Natura.Hooks.VALID_TREE_BIOMES; }
    public static boolean isValidTreeBiome(@Nonnull final Biome biome) {
        return Transformer_NetherHeight_Natura.Hooks.canGenerate(getValidTreeBiomes(), biome);
    }

    @Nonnull
    public static Set<Biome> getValidVineBiomes() { return Transformer_NetherHeight_Natura.Hooks.VALID_VINE_BIOMES; }
    public static boolean isValidVineBiome(@Nonnull final Biome biome) {
        return Transformer_NetherHeight_Natura.Hooks.canGenerate(getValidVineBiomes(), biome);
    }
}
