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

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.client.audio.NetherMusicHandler;
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
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    void buildSurface(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ, @Nonnull final ChunkPrimer primer, final int x, final int z, @Nonnull final double[] soulSandNoise, @Nonnull final double[] gravelNoise, @Nonnull final double[] depthBuffer, final double terrainNoise);

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     * @throws NullPointerException If chunkGenerator is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    default void populate(@Nonnull final INetherAPIChunkGenerator chunkGenerator, final int chunkX, final int chunkZ) {
        chunkGenerator.populateWithVanilla(chunkX, chunkZ);
    }

    /**
     * @return This biome's background fog color.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(final float celestialAngle, final float partialTicks) {
        return new Vec3d(0.2, 0.03, 0.03);
    }

    /**
     * @return The ambient music that plays while players are in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() {
        return NetherMusicHandler.DEFAULT_MUSIC.getMusicType();
    }

    /**
     * @return The boss music that plays while players are in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.4.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getBossMusicType() {
        return this.getMusicType();
    }

    /**
     * @return The creative mode music that plays while players are in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    default IMusicType getCreativeMusicType() {
        return this.getMusicType();
    }
}
