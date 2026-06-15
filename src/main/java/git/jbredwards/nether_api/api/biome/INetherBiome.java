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
 * Having your nether biome class implement this is heavily recommended, but not required.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface INetherBiome extends IMusicBiome
{
    /**
     * At the given x and z positions, build the biome surface by replacing the template blocks.
     */
    @ApiStatus.AvailableSince("1.0.0")
    void buildSurface(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise);

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    @ApiStatus.AvailableSince("1.0.0")
    default void populate(@Nonnull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return this biome's background fog color.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.2, 0.03, 0.03);
    }

    /**
     * @return the ambient music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() {
        return new VanillaMusicType(MusicTicker.MusicType.NETHER);
    }

    /**
     * @return the boss music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.4.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getBossMusicType() {
        return getMusicType();
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
