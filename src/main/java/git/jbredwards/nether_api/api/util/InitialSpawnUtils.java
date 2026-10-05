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

package git.jbredwards.nether_api.api.util;

import com.mojang.authlib.GameProfile;
import git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility functions related to Nether API's player spawning logic.<br>
 * For respawning, use functions found in {@link net.minecraft.entity.player.EntityPlayer}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public final class InitialSpawnUtils
{
    /**
     * @return The default initial spawn dimension for the active server.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static int getInitialSpawnDimension() {
        return PlayerSpawnLogic.getInitialSpawnDimension(null);
    }

    /**
     * @return The default initial spawn dimension for the given player profile.
     * @throws NullPointerException If profile is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static int getInitialSpawnDimension(@Nonnull final GameProfile profile) {
        return PlayerSpawnLogic.getInitialSpawnDimension(Objects.requireNonNull(profile));
    }

    /**
     * Sets the default player initial spawn dimension.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void setInitialSpawnDimension(final int initialSpawnDimension) {
        PlayerSpawnLogic.INITIAL_SPAWN_DIMENSION = initialSpawnDimension == 0 ? null : initialSpawnDimension;
    }

    /**
     * Sets special initial spawn dimensions for certain player profiles.
     * @param possibilities All possible nonnull values that could be returned by {@code factory}.
     * @param factory A function to decide the initial spawn dimension for any player profile.
     *                Returning null is allowed and will use the default initial spawn dimension.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.Experimental
    @ApiStatus.AvailableSince("1.5.0")
    public static void setInitialSpawnDimensionFactory(@Nonnull final IntSet possibilities, @Nonnull final Function<GameProfile, Integer> factory) {
        PlayerSpawnLogic.INITIAL_SPAWN_PER_PLAYER_POSSIBILITIES = Objects.requireNonNull(possibilities);
        PlayerSpawnLogic.INITIAL_SPAWN_PER_PLAYER = Objects.requireNonNull(factory);
    }
}
