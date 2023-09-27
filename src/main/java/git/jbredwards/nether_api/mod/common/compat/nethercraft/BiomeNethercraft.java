/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.nethercraft;

import com.legacy.nethercraft.blocks.BlocksNether;
import com.legacy.nethercraft.world.NetherGenReeds;
import com.legacy.nethercraft.world.NetherGenTree;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.block.INetherCarvable;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
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
    BiomeNethercraft() {
        super(new BiomeProperties("Glowing Grove").setTemperature(2).setRainfall(0).setRainDisabled());
        setRegistryName(NetherAPI.MODID, "nethercraft_glowing_grove");

        topBlock = BlocksNether.nether_dirt.getDefaultState();
        fillerBlock = Blocks.NETHERRACK.getDefaultState();
    }

    @Override
    public void buildSurface(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise) {
        NetherGenerationUtils.buildSurfaceAndSoulSandGravel(chunkGenerator.getWorld(), chunkGenerator.getRand(), primer, x, z, soulSandNoise, gravelNoise, depthBuffer,
                Blocks.NETHERRACK.getDefaultState(), topBlock, fillerBlock, Blocks.LAVA.getDefaultState(), BlocksNether.heat_sand.getDefaultState(), BlocksNether.heat_sand.getDefaultState());
    }

    @Override
    public void decorate(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos) {
        super.decorate(worldIn, rand, pos);
        // nether trees
        final int treeTries = 35 + rand.nextInt(5);
        for(int i = 0; i < treeTries; i++) getRandomTreeFeature(rand).generate(worldIn, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
        // nether reeds
        for(int i = 0; i < 7; i++) if(rand.nextInt(10) == 0) new NetherGenReeds().generate(worldIn, rand, pos.add(rand.nextInt(16) + 8, 32, rand.nextInt(16) + 8));
        // glowing mushrooms
        for(int i = 0; i < 3; i++) {
            BlockPos mushroomPos = null;
            if(rand.nextInt(4) == 0) new WorldGenBush((BlockBush)BlocksNether.purple_glowshroom).generate(worldIn, rand, mushroomPos = pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
            if(rand.nextInt(8) == 0) new WorldGenBush((BlockBush)BlocksNether.green_glowshroom).generate(worldIn, rand, mushroomPos != null ? mushroomPos : pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
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
