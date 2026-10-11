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
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The default implementation of {@link ISoundAmbience}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public class SoundAmbience implements ISoundAmbience
{
    @Nonnull
    protected final SoundEvent soundEvent;
    protected final double chancePerTick;

    @ApiStatus.AvailableSince("1.0.0")
    public SoundAmbience(@Nonnull final SoundEvent soundEventIn, final double chancePerTickIn) {
        this.soundEvent = Objects.requireNonNull(soundEventIn);
        this.chancePerTick = chancePerTickIn;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() { return this.soundEvent; }

    @Override
    public double getChancePerTick() { return this.chancePerTick; }
}
