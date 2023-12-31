/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.audio.impl.VanillaMusicType;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Having your nether biome class implement this is heavily recommended, but not required.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherBiome
{
    /**
     * At the given x and z positions, build the biome surface by replacing the template blocks.
     */
    void buildSurface(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise);

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    default void populate(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return this biome's background fog color.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(float celestialAngle, float partialTicks) { return new Vec3d(0.2, 0.03, 0.03); }

    /**
     * @return the ambient music that plays while players are in this biome.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() { return new VanillaMusicType(MusicTicker.MusicType.NETHER); }
}
