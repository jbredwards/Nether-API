/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.audio;

import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 * Allows biomes to have their own ambient sounds.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ISoundAmbience
{
    /**
     * @return the SoundEvent that gets played.
     */
    @Nonnull
    SoundEvent getSoundEvent();

    /**
     * @return the chance per tick that this randomly plays.
     */
    double getChancePerTick();
}
