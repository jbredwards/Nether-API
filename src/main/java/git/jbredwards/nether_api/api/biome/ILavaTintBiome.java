/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.biome;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Biomes should implement this if they want lava to be tinted a different color.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ILavaTintBiome
{
    /**
     * @return the tint color of lava in this biome.
     * @since 1.3.0
     */
    int getBiomeLavaColor(@Nonnull final BlockPos lavaPos);
}
