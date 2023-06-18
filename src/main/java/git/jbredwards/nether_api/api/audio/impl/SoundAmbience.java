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
