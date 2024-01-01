/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.nethercraft;

import com.legacy.nethercraft.blocks.BlocksNether;
import com.legacy.nethercraft.world.NetherGenReeds;
import com.legacy.nethercraft.world.NetherGenTree;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.block.INetherCarvable;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeHell;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBush;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public final class BiomeNethercraft extends BiomeHell implements INetherBiome, INetherCarvable
{
    // mutable via GroovyScript
    public static int minTreeTries = 35, maxTreeTries = 39, reedTries = 7, reedChance = 10, mushroomTries = 3, purpleMushroomChance = 4, greenMushroomChance = 8;
    BiomeNethercraft() {
        super(new BiomeProperties("Glowing Grove").setTemperature(2).setRainfall(0).setRainDisabled());
        setRegistryName(NetherAPI.MODID, "nethercraft_glowing_grove");
        topBlock = BlocksNether.nether_dirt.getDefaultState();
    }

    @Override
    public void buildSurface(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise) {
        // copied from nethercraft mod, to ensure terrain generates the same

        final boolean soulSand = soulSandNoise[x << 4 | z] + chunkGenerator.getRand().nextDouble() * 0.2 > 0;
        final boolean gravel = gravelNoise[x << 4 | z] + chunkGenerator.getRand().nextDouble() * 0.2 > 0;
        final int depth = (int)(depthBuffer[x << 4 | z] / 3 + 3 + chunkGenerator.getRand().nextDouble() * 0.25);
        @Nonnull IBlockState filler = topBlock;

        int depthRemaining = -1;
        for(int y = chunkGenerator.getWorld().getActualHeight() - 1; y >= 0; --y) {
            if(y < chunkGenerator.getWorld().getActualHeight() - 1 - chunkGenerator.getRand().nextInt(5) && y > chunkGenerator.getRand().nextInt(5)) {
                if(primer.getBlockState(x, y, z).getBlock() == Blocks.NETHERRACK) {
                    if(depthRemaining == -1) {
                        if(depth <= 0) filler = Blocks.AIR.getDefaultState();
                        else if(y > chunkGenerator.getWorld().getSeaLevel() - 4 && y < chunkGenerator.getWorld().getSeaLevel() + 3)
                            filler = gravel || soulSand ? BlocksNether.heat_sand.getDefaultState() : topBlock;

                        depthRemaining = depth;
                        primer.setBlockState(x, y, z, y >= chunkGenerator.getWorld().getSeaLevel() ? filler : topBlock);
                    }
                    else if(depthRemaining > 0) {
                        --depthRemaining;
                        primer.setBlockState(x, y, z, topBlock);
                    }
                }

                else depthRemaining = -1;
            }
        }
    }

    @Override
    public void decorate(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos) {
        super.decorate(worldIn, rand, pos);
        final int worldHeight = worldIn.getActualHeight();

        // nether trees
        final int treeTries = MathHelper.getInt(rand, minTreeTries, maxTreeTries); // << (worldHeight >> 8);
        for(int i = 0; i < treeTries; i++) getRandomTreeFeature(rand).generate(worldIn, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(worldHeight - 4) + 4, rand.nextInt(16) + 8));

        // nether reeds
        if(reedChance > 0) {
            for(int i = 0; i < reedTries; i++) {
                if(rand.nextInt(reedChance) == 0) new NetherGenReeds().generate(worldIn, rand, pos.add(rand.nextInt(16) + 8, 32, rand.nextInt(16) + 8));
            }
        }

        // glowing mushrooms
        if(Math.max(purpleMushroomChance, greenMushroomChance) > 0) {
            final int tries = mushroomTries; // << (worldHeight >> 8);
            for(int i = 0; i < tries; i++) {
                BlockPos mushroomPos = null;
                if(purpleMushroomChance > 0 && rand.nextInt(purpleMushroomChance) == 0) new WorldGenBush((BlockBush)BlocksNether.purple_glowshroom)
                        .generate(worldIn, rand, mushroomPos = pos.add(rand.nextInt(16) + 8, rand.nextInt(worldHeight - 4) + 4, rand.nextInt(16) + 8));
                if(greenMushroomChance > 0 && rand.nextInt(greenMushroomChance) == 0) new WorldGenBush((BlockBush)BlocksNether.green_glowshroom)
                        .generate(worldIn, rand, mushroomPos != null ? mushroomPos : pos.add(rand.nextInt(16) + 8, rand.nextInt(worldHeight - 4) + 4, rand.nextInt(16) + 8));
            }
        }
    }

    @Override
    public boolean canNetherCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z) {
        return state == fillerBlock || state == topBlock || state.getBlock() == BlocksNether.heat_sand;
    }

    @Nonnull
    @Override
    public WorldGenAbstractTree getRandomTreeFeature(@Nonnull Random rand) { return new NetherGenTree(); }
}
