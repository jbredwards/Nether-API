/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

import javax.annotation.Nonnull;

/**
 * Having your Block (or Biome) implement this allows it to have custom behavior when a nether cave generates through it.
 * One use for this would be to allow nether caves to generate through your blocks.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherCarvable
{
    /**
     * Fired when a nether cave tries to generate over this block.
     */
    boolean canNetherCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z);
}
