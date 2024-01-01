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
 * Having your end biome class implement this is heavily recommended, but not required.
 *
 * @since 1.3.0
 * @author jbred
 *
 */
public interface IEndBiome
{
    /**
     * At the given x and z positions, build the biome surface by replacing the template blocks.
     */
    void buildSurface(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, @Nonnull final ChunkPrimer primer, final int x, final int z, final double terrainNoise);

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    default void populate(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return true if this biome can generate small floating islands.
     */
    default boolean generateIslands(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        return true;
    }

    /**
     * @return true if this biome can generate chorus plants.
     */
    default boolean generateChorusPlants(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        return true;
    }

    /**
     * @return true if this biome can generate end cities.
     */
    default boolean generateEndCity(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final int islandHeight) {
        return islandHeight >= 60;
    }

    /**
     * @return true if this biome creates extra fog.
     */
    @SideOnly(Side.CLIENT)
    default boolean hasExtraXZFog(@Nonnull final World world, final int x, final int z) { return false; }

    /**
     * @return this biome's background fog color.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(final float celestialAngle, final float partialTicks) { return new Vec3d(0.09411766, 0.07529412, 0.09411766); }

    /**
     * @return the ambient music that plays while players are in this biome.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() { return new VanillaMusicType(MusicTicker.MusicType.END); }

    /**
     * @return the boss music that plays while players are in this biome.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default IMusicType getBossMusicType() { return new VanillaMusicType(MusicTicker.MusicType.END_BOSS); }
}
