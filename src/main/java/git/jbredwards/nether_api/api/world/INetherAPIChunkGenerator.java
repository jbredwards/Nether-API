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

package git.jbredwards.nether_api.api.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Provides access to certain {@link IChunkGenerator} features.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface INetherAPIChunkGenerator extends IChunkGenerator
{
    /**
     * @return The World instance.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    World getWorld();

    /**
     * @return The Random instance.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    Random getRand();

    /**
     * @return Whether the world settings have structures enabled.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    boolean areStructuresEnabled();

    /**
     * Utility function that runs the same code vanilla uses to populate its chunks.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    void populateWithVanilla(final int chunkX, final int chunkZ);

    /**
     * Utility function that populates the provided ChunkPrimer with the basic terrain blocks.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.3.0")
    void setBlocksInPrimer(final int chunkX, final int chunkZ, @Nonnull final ChunkPrimer primer);
}
