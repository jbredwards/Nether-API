/*
 * Copyright (C) <2026 to Present> <jbredwards>
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
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Having your Block implement this allows it to control whether its block particles will be colored.
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public interface IConditionalParticleColor
{
    /**
     * @param state The block state owning the particle.
     * @return True if the particle can use the block state's color multiplier.
     * @throws NullPointerException If state is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    boolean particleUseBlockColor(@Nonnull final IBlockState state);
}
