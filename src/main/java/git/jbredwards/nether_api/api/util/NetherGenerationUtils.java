package git.jbredwards.nether_api.api.util;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Provides some utility functions for generating nether features.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public final class NetherGenerationUtils
{
    /**
     * At the given x and z positions, replaces "stateToFill" with the provided top and filler blocks. Also generates the random gravel and soul sand for the biome.
     */
    public static void buildSurfaceAndSoulSandGravel(@Nonnull World world, @Nonnull Random rand, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, @Nonnull IBlockState stateToFill, @Nonnull IBlockState topBlockIn, @Nonnull IBlockState fillerBlockIn) {
        final boolean soulSand = (!NetherAPI.isNetherExLoaded || NetherExHandler.doesSoulSandGenerate()) && soulSandNoise[z + (x << 4)] + rand.nextDouble() * 0.2 > 0;
        final boolean gravel = (!NetherAPI.isNetherExLoaded || NetherExHandler.doesGravelGenerate()) && gravelNoise[z + (x << 4)] + rand.nextDouble() * 0.2 > 0;
        final int depth = (int)(depthBuffer[z + (x << 4)] / 3 + 3 + rand.nextDouble() * 0.25);
        int depthRemaining = -1;

        final int seaLevel = world.getSeaLevel() + 1;
        IBlockState topBlock = topBlockIn;
        IBlockState fillerBlock = fillerBlockIn;

        for(int y = 127; y >= 0; --y) {
            final IBlockState checkState = primer.getBlockState(x, y, z);
            if(checkState.getMaterial() != Material.AIR) {
                if(checkState == stateToFill) {
                    if(depthRemaining == -1) {
                        if(depth <= 0) fillerBlock = fillerBlockIn;
                        else if(y >= seaLevel - 4 && y <= seaLevel + 1) {
                            topBlock = topBlockIn;
                            fillerBlock = fillerBlockIn;

                            if(gravel) topBlock = Blocks.GRAVEL.getDefaultState();
                            if(soulSand) {
                                topBlock = Blocks.SOUL_SAND.getDefaultState();
                                fillerBlock = Blocks.SOUL_SAND.getDefaultState();
                            }
                        }

                        if(y < seaLevel && topBlock.getMaterial() == Material.AIR) {
                            topBlock = Blocks.LAVA.getDefaultState();
                        }

                        depthRemaining = depth;
                        if(y >= seaLevel - 1) primer.setBlockState(x, y, z, topBlock);
                        else primer.setBlockState(x, y, z, fillerBlock);
                    }

                    else if(depthRemaining > 0) {
                        --depthRemaining;
                        primer.setBlockState(x, y, z, fillerBlock);
                    }
                }
            }

            else depthRemaining = -1;
        }
    }

    public static void generateVanillaNetherFeatures(@Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, boolean generateStructures, boolean canSupportFortress) {

    }
}