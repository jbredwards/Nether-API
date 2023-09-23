/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.common.biome.nether.BOPHellBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.block.INetherCarvable;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused") //used via asm
public abstract class AbstractNetherBOPBiome extends BOPHellBiome implements INetherBiome, INetherCarvable
{
    public AbstractNetherBOPBiome(@Nonnull String idName, @Nonnull PropsBuilder defaultBuilder) {
        super(idName, defaultBuilder);
    }

    @Override
    public boolean canNetherCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z) {
        return topBlock == state || fillerBlock == state || wallBlock == state || roofTopBlock == state || roofFillerBlock == state;
    }

    @Override
    public void buildSurface(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise) {
        NetherGenerationUtils.buildSurfaceAndSoulSandGravel(chunkGenerator.getWorld(), chunkGenerator.getRand(), primer, x, z, soulSandNoise, gravelNoise, depthBuffer, Blocks.NETHERRACK.getDefaultState(), Blocks.NETHERRACK.getDefaultState(), Blocks.NETHERRACK.getDefaultState(), Blocks.LAVA.getDefaultState());
        genTerrainBlocks(chunkGenerator.getWorld(), chunkGenerator.getRand(), primer, chunkZ << 4 | z, chunkX << 4 | x, terrainNoise); //BOP swaps the x and z coords (for some reason??? it caused me a lot of pain until I realized ;-;)
    }

    //heavily copied from ChunkGeneratorHellBOP to ensure BOP biomes generate as authentically as possible
    @Override
    public void populate(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ) {
        final World world = chunkGenerator.getWorld();
        final Random rand = chunkGenerator.getRand();
        final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);

        ForgeEventFactory.onChunkPopulate(true, chunkGenerator, world, rand, chunkX, chunkZ, false);
        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
            for (int k = 0; k < 8; ++k)
                new WorldGenHellLava(Blocks.FLOWING_LAVA, false).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));

        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, pos));

        // note: this was moved earlier to be more similar to overworld biome decoration, however
        // it's possible that this may cause issues with other mods
        decorate(world, rand, pos);

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.FIRE))
            for (int i1 = 0; i1 < rand.nextInt(rand.nextInt(10) + 1) + 1; ++i1) {
                new WorldGenFire().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
            }

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.GLOWSTONE)) {
            for (int j1 = 0; j1 < rand.nextInt(rand.nextInt(10) + 1); ++j1) {
                new WorldGenGlowStone1().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
            }

            for (int k1 = 0; k1 < 10; ++k1) {
                new WorldGenGlowStone2().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }
        }//Forge: End doGlowstone

        ForgeEventFactory.onChunkPopulate(false, chunkGenerator, world, rand, chunkX, chunkZ, false);

        if (TerrainGen.decorate(world, rand, pos, DecorateBiomeEvent.Decorate.EventType.SHROOM)) {
            if (rand.nextBoolean()) {
                new WorldGenBush(Blocks.BROWN_MUSHROOM).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }

            if (rand.nextBoolean()) {
                new WorldGenBush(Blocks.RED_MUSHROOM).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }
        }

        final WorldGenerator quartzGen = new WorldGenMinable(Blocks.QUARTZ_ORE.getDefaultState(), 14, BlockMatcher.forBlock(Blocks.NETHERRACK));
        if (TerrainGen.generateOre(world, rand, quartzGen, pos, OreGenEvent.GenerateMinable.EventType.QUARTZ))
            for (int l1 = 0; l1 < 16; ++l1) {
                quartzGen.generate(world, rand, pos.add(rand.nextInt(16), rand.nextInt(108) + 10, rand.nextInt(16)));
            }

        int i2 = world.getSeaLevel() / 2 + 1;

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
            for (int l = 0; l < 4; ++l) {
                new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33, BlockMatcher.forBlock(Blocks.NETHERRACK))
                        .generate(world, rand, pos.add(rand.nextInt(16), i2 - 5 + rand.nextInt(10), rand.nextInt(16)));
            }

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
            for (int j2 = 0; j2 < 16; ++j2) {
                new WorldGenHellLava(Blocks.FLOWING_LAVA, true).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(108) + 10, rand.nextInt(16) + 8));
            }

        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, pos));
    }
}
