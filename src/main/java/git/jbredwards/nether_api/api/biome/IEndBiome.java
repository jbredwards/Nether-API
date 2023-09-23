/*
 * Copyright (c) 2023. jbredwards
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
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    default void populate(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return true if this biome can generate chorus plants.
     */
    default boolean generateChorusPlants(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ) {
        return true;
    }

    /**
     * It's also recommended to override {@link net.minecraft.world.biome.Biome#getSkyColorByTemp Biome.getSkyColorByTemp()} for the best result.
     * @return this biome's background fog color.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(float celestialAngle, float partialTicks) { return new Vec3d(0.777451, 0.6519608, 0.777451); }

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
