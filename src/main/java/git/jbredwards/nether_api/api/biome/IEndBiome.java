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

package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IMusicBiome;
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
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Having your end biome class implement this is heavily recommended, but not required.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.3.0")
public interface IEndBiome extends IMusicBiome
{
    /**
     * At the given x and z positions, build the biome surface by replacing the template blocks.
     */
    @ApiStatus.AvailableSince("1.3.0")
    void buildSurface(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, @Nonnull final ChunkPrimer primer, final int x, final int z, final double terrainNoise);

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    @ApiStatus.AvailableSince("1.3.0")
    default void populate(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return true if this biome can generate small floating islands.
     */
    @ApiStatus.AvailableSince("1.3.0")
    default boolean generateIslands(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        return true;
    }

    /**
     * @return true if this biome can generate chorus plants.
     */
    @ApiStatus.AvailableSince("1.3.0")
    default boolean generateChorusPlants(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final float islandHeight) {
        return true;
    }

    /**
     * @return true if this biome can generate end cities.
     */
    @ApiStatus.AvailableSince("1.3.0")
    default boolean generateEndCity(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, final int islandHeight) {
        return islandHeight >= 60;
    }

    /**
     * @return true if this biome creates extra fog.
     */
    @ApiStatus.AvailableSince("1.3.0")
    @SideOnly(Side.CLIENT)
    default boolean hasExtraXZFog(@Nonnull final World world, final int x, final int z) {
        return false;
    }

    /**
     * @return this biome's background fog color.
     */
    @ApiStatus.AvailableSince("1.3.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(final float celestialAngle, final float partialTicks) {
        return new Vec3d(0.09411766, 0.07529412, 0.09411766);
    }

    /**
     * @return the ambient music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.3.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() {
        return new VanillaMusicType(MusicTicker.MusicType.END);
    }

    /**
     * @return the boss music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.3.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getBossMusicType() {
        return new VanillaMusicType(MusicTicker.MusicType.END_BOSS);
    }

    /**
     * @return the creative mode music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getCreativeMusicType() {
        return getMusicType();
    }
}
