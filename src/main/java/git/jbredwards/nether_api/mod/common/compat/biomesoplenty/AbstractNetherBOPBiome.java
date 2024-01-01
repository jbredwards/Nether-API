/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.common.biome.nether.BOPHellBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.block.INetherCarvable;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

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

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        final int fogColor = getFogColor(null);
        if(fogColor != -1) { // custom fog color set via config
            final double r = (double)(fogColor >> 16 & 0xFF) / 255;
            final double g = (double)(fogColor >> 8 & 0xFF) / 255;
            final double b = (double)(fogColor & 0xFF) / 255;
            return new Vec3d(r, g, b);
        }

        return INetherBiome.super.getFogColor(celestialAngle, partialTicks);
    }
}
