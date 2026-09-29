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

package git.jbredwards.nether_api.api.audio;

import git.jbredwards.nether_api.api.audio.impl.SoundAmbience;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Allows biomes to have their own ambient sounds.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ISoundAmbience
{
    /**
     * The chance that's used by Vanilla in modern versions.
     */
    @ApiStatus.AvailableSince("1.5.0")
    double DEFAULT_CHANCE_PER_TICK = 1 / (4.5 * 20);

    /**
     * @return The SoundEvent that gets played.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    SoundEvent getSoundEvent();

    /**
     * @return The chance per tick that this randomly plays.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    double getChancePerTick();

    /**
     * @return A new
     * @throws NullPointerException If soundEvent is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static SoundAmbience of(@Nonnull final SoundEvent soundEvent) {
        return ISoundAmbience.of(soundEvent, ISoundAmbience.DEFAULT_CHANCE_PER_TICK);
    }

    /**
     * @return
     * @throws NullPointerException If soundEvent is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static SoundAmbience of(@Nonnull final SoundEvent soundEvent, final double chancePerTick) {
        return new SoundAmbience(soundEvent, chancePerTick);
    }
}
