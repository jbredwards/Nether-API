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

import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Allows biomes to have their own darkness ambient sound (cave sounds).
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface IDarkSoundAmbience extends ISoundAmbience
{
    /**
     * The chance that's used by Vanilla in modern versions.
     */
    @ApiStatus.AvailableSince("1.5.0")
    double DEFAULT_CHANCE_PER_TICK = 1 / (300.0 * 20);

    /**
     * The light search radius that's used by Vanilla in modern versions.
     */
    @ApiStatus.AvailableSince("1.5.0")
    int DEFAULT_LIGHT_SEARCH_RADIUS = 8;

    /**
     * The sound offset that's used by Vanilla in modern versions.
     */
    @ApiStatus.AvailableSince("1.5.0")
    double DEFAULT_SOUND_OFFSET = 2;

    /**
     * @return The radius that can be scanned for light values.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    int getLightSearchRadius();

    /**
     * @return The general distance away from the player where the sound will be played.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    double getSoundOffset();

    /**
     * @return A new {@code IDarkSoundAmbience} instance with Vanilla ambience properties.
     * @throws NullPointerException If soundEvent is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static DarkSoundAmbience vanilla(@Nonnull final SoundEvent soundEvent) {
        return new DarkSoundAmbience(soundEvent, IDarkSoundAmbience.DEFAULT_CHANCE_PER_TICK, IDarkSoundAmbience.DEFAULT_LIGHT_SEARCH_RADIUS, IDarkSoundAmbience.DEFAULT_SOUND_OFFSET);
    }

    /**
     * @return A new {@code IDarkSoundAmbience} instance with custom ambience properties.
     * @throws NullPointerException If soundEvent is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static DarkSoundAmbience modded(@Nonnull final SoundEvent soundEvent, final double chancePerTick, final int lightSearchRadius, final double soundOffset) {
        return new DarkSoundAmbience(soundEvent, chancePerTick, lightSearchRadius, soundOffset);
    }
}
