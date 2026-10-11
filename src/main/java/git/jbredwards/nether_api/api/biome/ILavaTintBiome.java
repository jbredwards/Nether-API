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

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Biomes should implement this if they want lava to be tinted a different color.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ILavaTintBiome
{
    /**
     * Has a default implementation since v1.5.0.
     *
     * @return The tint color of lava in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.3.0")
    default int getBiomeLavaColor(@Nonnull final BlockPos lavaPos) {
        return getBiomeLavaColor();
    }

    /**
     * Has a default implementation since v1.5.0.
     *
     * @return The tint color of lava in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("[1.0.0,1.2.0],[1.5.0,)")
    @ApiStatus.OverrideOnly
    default int getBiomeLavaColor() {
        return -1;
    }
}
