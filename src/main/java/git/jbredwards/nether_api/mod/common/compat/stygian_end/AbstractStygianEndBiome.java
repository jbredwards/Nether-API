/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.stygian_end;

import fluke.stygian.block.ModBlocks;
import fluke.stygian.world.BiomeRegistrar;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused") //used via asm
public abstract class AbstractStygianEndBiome extends BiomeEnd implements IEndBiome
{
    public AbstractStygianEndBiome(@Nonnull final BiomeProperties properties) { super(properties); }

    @Override
    public void buildSurface(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, @Nonnull final ChunkPrimer primer, final int x, final int z, final double terrainNoise) {
        // copied from stygian end mod, to ensure terrain generates the same
        int currDepth = -1;
        for(int y = chunkGenerator.getWorld().getActualHeight() - 1; y >= 0; --y) {
            final IBlockState here = primer.getBlockState(x, y, z);
            if(here.getMaterial() == Material.AIR) currDepth = -1;
            else if(here.getBlock() == Blocks.END_STONE) {
                if(currDepth == -1) {
                    currDepth = 3 + chunkGenerator.getRand().nextInt(2);
                    primer.setBlockState(x, y, z, topBlock);
                }
                else if(currDepth > 0) {
                    --currDepth;
                    primer.setBlockState(x, y, z, fillerBlock);
                }
            }
        }
    }

    @Override
    public boolean generateChorusPlants(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        return this == BiomeRegistrar.END_JUNGLE ? NetherAPIConfig.StygianEnd.endJungleChorusPlants : NetherAPIConfig.StygianEnd.endVolcanoChorusPlants;
    }

    @Override
    public boolean generateEndCity(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final int islandHeight) {
        return islandHeight >= 60 && (this == BiomeRegistrar.END_JUNGLE ? NetherAPIConfig.StygianEnd.endJungleEndCity : NetherAPIConfig.StygianEnd.endVolcanoEndCity);
    }

    @Override
    public boolean generateIslands(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        // generate islands out of end obsidian 50% of the time
        if(this == BiomeRegistrar.END_VOLCANO) {
            if(chunkGenerator.getRand().nextBoolean()) {
                if(islandHeight < -20 && chunkGenerator.getRand().nextInt(14) == 0) {
                    @Nonnull final WorldGenerator feature = new WorldGenerator() {
                        @Override
                        public boolean generate(@Nonnull final World worldIn, @Nonnull final Random rand, @Nonnull final BlockPos position) {
                            float radius = rand.nextInt(3) + 4;
                            for(int y = 0; radius > 0.5; y--) {
                                for(int x = MathHelper.floor(-radius); x <= MathHelper.ceil(radius); x++) {
                                    for(int z = MathHelper.floor(-radius); z <= MathHelper.ceil(radius); z++) {
                                        if((float)(x * x + z * z) <= (radius + 1) * (radius + 1)) setBlockAndNotifyAdequately(worldIn, position.add(x, y, z), ModBlocks.endObsidian.getDefaultState());
                                    }
                                }

                                radius -= rand.nextInt(2) + 0.5;
                            }

                            return true;
                        }
                    };

                    feature.generate(chunkGenerator.getWorld(), chunkGenerator.getRand(), new BlockPos((chunkX << 4) + chunkGenerator.getRand().nextInt(16) + 8, chunkGenerator.getRand().nextInt(16) + 55, (chunkZ << 4) + chunkGenerator.getRand().nextInt(16) + 8));
                    if(chunkGenerator.getRand().nextInt(4) == 0) feature.generate(chunkGenerator.getWorld(), chunkGenerator.getRand(), new BlockPos((chunkX << 4) + chunkGenerator.getRand().nextInt(16) + 8, chunkGenerator.getRand().nextInt(16) + 55, (chunkZ << 4) + chunkGenerator.getRand().nextInt(16) + 8));
                }

                return false;
            }
        }

        // don't generate islands too close to the main land
        else return islandHeight < -25;
        return true;
    }
}
