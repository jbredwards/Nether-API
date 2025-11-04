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

import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 * The default implementation of {@link ISoundAmbience}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public class SoundAmbience implements ISoundAmbience
{
    @Nonnull
    protected final SoundEvent soundEvent;
    protected final double chancePerTick;

    public SoundAmbience(@Nonnull SoundEvent soundEventIn, double chancePerTickIn) {
        soundEvent = soundEventIn;
        chancePerTick = chancePerTickIn;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() { return soundEvent; }

    @Override
    public double getChancePerTick() { return chancePerTick; }
}
