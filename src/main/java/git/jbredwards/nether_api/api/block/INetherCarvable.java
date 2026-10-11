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

package git.jbredwards.nether_api.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Having your Block (or Biome) implement this allows it to have custom behavior when a nether cave generates through it.
 * One use for this would be to allow nether caves to generate through your blocks.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface INetherCarvable
{
    /**
     * Fired when a nether cave tries to generate over this block.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    boolean canNetherCarveThrough(@Nonnull final IBlockState state, @Nonnull final ChunkPrimer primer, final int x, final int y,final int z);
}
