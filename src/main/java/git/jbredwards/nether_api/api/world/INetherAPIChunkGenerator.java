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

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Provides access to certain {@link IChunkGenerator} features.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherAPIChunkGenerator extends IChunkGenerator
{
    /**
     * @return the World instance.
     */
    @Nonnull
    World getWorld();

    /**
     * @return the Random instance.
     */
    @Nonnull
    Random getRand();

    /**
     * @return true if the world settings have structures enabled.
     */
    boolean areStructuresEnabled();

    /**
     * Utility function that runs the same code vanilla uses to populate its chunks.
     */
    void populateWithVanilla(int chunkX, int chunkZ);

    /**
     * Utility function that populates the provided ChunkPrimer with the basic terrain blocks.
     * @since 1.3.0
     */
    void setBlocksInPrimer(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer);
}
