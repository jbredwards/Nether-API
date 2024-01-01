/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.audio.impl;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 * The default implementation of {@link IDarkSoundAmbience}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public class DarkSoundAmbience extends SoundAmbience implements IDarkSoundAmbience
{
    @Nonnull
    public static final DarkSoundAmbience DEFAULT_CAVE = new DarkSoundAmbience(SoundEvents.AMBIENT_CAVE, 1.0 / 6000, 8, 2);

    protected final int lightSearchRadius;
    protected final double soundOffset;

    public DarkSoundAmbience(@Nonnull SoundEvent soundEventIn, double chancePerTickIn, int lightSearchRadiusIn, double soundOffsetIn) {
        super(soundEventIn, chancePerTickIn);
        lightSearchRadius = lightSearchRadiusIn;
        soundOffset = soundOffsetIn;
    }

    @Override
    public int getLightSearchRadius() { return lightSearchRadius; }

    @Override
    public double getSoundOffset() { return soundOffset; }
}
