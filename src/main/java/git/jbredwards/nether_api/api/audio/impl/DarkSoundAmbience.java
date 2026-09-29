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

package git.jbredwards.nether_api.api.audio.impl;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * The default implementation of {@link IDarkSoundAmbience}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public class DarkSoundAmbience extends SoundAmbience implements IDarkSoundAmbience
{
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    public static final DarkSoundAmbience DEFAULT_CAVE = IDarkSoundAmbience.vanilla(SoundEvents.AMBIENT_CAVE);

    protected final int lightSearchRadius;
    protected final double soundOffset;

    @ApiStatus.AvailableSince("1.0.0")
    public DarkSoundAmbience(@Nonnull SoundEvent soundEventIn, double chancePerTickIn, int lightSearchRadiusIn, double soundOffsetIn) {
        super(soundEventIn, chancePerTickIn);
        lightSearchRadius = lightSearchRadiusIn;
        soundOffset = soundOffsetIn;
    }

    @Override
    public int getLightSearchRadius() {
        return lightSearchRadius;
    }

    @Override
    public double getSoundOffset() {
        return soundOffset;
    }
}
