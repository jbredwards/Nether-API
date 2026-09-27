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

package git.jbredwards.nether_api.api.util;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Provides some utility functions for generating world features.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public final class NetherGenerationUtils
{
    /**
     * Nether generation utility that, at the given x and z positions, replaces "stateToFill" with the provided top and filler blocks. Also generates the random gravel and soul sand for the biome.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    public static void buildSurfaceAndSoulSandGravel(@Nonnull World world, @Nonnull Random rand, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, @Nonnull IBlockState stateToFill, @Nonnull IBlockState topBlockIn, @Nonnull IBlockState fillerBlockIn, @Nonnull IBlockState liquidBlockIn) {
        buildSurfaceAndSoulSandGravel(world, rand, primer, x, z, soulSandNoise, gravelNoise, depthBuffer, stateToFill, topBlockIn, fillerBlockIn, liquidBlockIn, Blocks.GRAVEL.getDefaultState(), Blocks.SOUL_SAND.getDefaultState());
    }

    /**
     * Nether generation utility that, at the given x and z positions, replaces "stateToFill" with the provided top and filler blocks. Also generates the random gravel and soul sand for the biome.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.3.0")
    public static void buildSurfaceAndSoulSandGravel(@Nonnull World world, @Nonnull Random rand, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, @Nonnull IBlockState stateToFill, @Nonnull IBlockState topBlockIn, @Nonnull IBlockState fillerBlockIn, @Nonnull IBlockState liquidBlockIn, @Nonnull IBlockState gravelIn, @Nonnull IBlockState sandIn) {
        final boolean soulSand = (!NetherAPI.isNetherExLoaded || NetherExHandler.doesSoulSandGenerate() || sandIn.getBlock() != Blocks.SOUL_SAND) && soulSandNoise[x << 4 | z] + rand.nextDouble() * 0.2 > 0;
        final boolean gravel = (!NetherAPI.isNetherExLoaded || NetherExHandler.doesGravelGenerate() || gravelIn.getBlock() != Blocks.GRAVEL) && gravelNoise[x << 4 | z] + rand.nextDouble() * 0.2 > 0;
        final int depth = (int)(depthBuffer[x << 4 | z] / 3 + 3 + rand.nextDouble() * 0.25);
        int depthRemaining = -1;

        final int seaLevel = world.getSeaLevel() + 1;
        IBlockState topBlock = topBlockIn;
        IBlockState fillerBlock = fillerBlockIn;

        IBlockState prevCheckState = Blocks.AIR.getDefaultState();
        for(int y = world.getActualHeight() - 1; y >= 0; --y) {
            final IBlockState checkState = primer.getBlockState(x, y, z);
            if(checkState.getMaterial() != Material.AIR) {
                if(checkState == stateToFill) {
                    if(depthRemaining == -1) {
                        if(depth <= 0) fillerBlock = fillerBlockIn;
                        else if(y >= seaLevel - 4 && y <= seaLevel + 1) {
                            topBlock = topBlockIn;
                            fillerBlock = fillerBlockIn;

                            if(gravel) topBlock = gravelIn;
                            if(soulSand) {
                                topBlock = sandIn;
                                fillerBlock = sandIn;
                            }
                        }

                        if(y < seaLevel && topBlock.getMaterial() == Material.AIR) {
                            topBlock = liquidBlockIn;
                        }

                        depthRemaining = depth;
                        if(prevCheckState.getMaterial() == Material.AIR) {
                            primer.setBlockState(x, y, z, topBlock);
                            prevCheckState = topBlock;
                        }

                        else {
                            primer.setBlockState(x, y, z, fillerBlock);
                            prevCheckState = fillerBlock;
                        }
                    }

                    else if(depthRemaining > 0) {
                        --depthRemaining;
                        primer.setBlockState(x, y, z, fillerBlock);
                        prevCheckState = fillerBlock;
                    }

                    else prevCheckState = checkState;
                }

                else prevCheckState = checkState;
            }

            else {
                depthRemaining = -1;
                prevCheckState = checkState;
            }
        }
    }

    /**
     * @return A new Random whose seed is based on a combination of the world seed + chunk pos.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.4.0")
    @Nonnull
    public static Random createSeedRandom(final long seed, final int chunkX, final int chunkZ) {
        @Nonnull final Random rand = new Random(seed);
        rand.setSeed(((rand.nextLong() >> 2 + 1) * chunkX + (rand.nextLong() >> 2 + 1) * chunkZ) ^ seed);
        return rand;
    }

    /**
     * Exposes {@link ChunkGeneratorEnd#getIslandHeightValue} for other mods to use.
     * @return Noise height of the end island at the provided chunk position.
     * @throws NullPointerException If generator is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static float getEndIslandHeightValue(@Nonnull final ChunkGeneratorEnd generator, final int chunkX, final int chunkZ) {
        return generator.getIslandHeightValue(chunkX, chunkZ, 1, 1);
    }
}
