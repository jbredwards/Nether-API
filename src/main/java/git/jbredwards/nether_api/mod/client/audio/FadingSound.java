/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Heavily inspired by Vanilla 1.16's <B>BiomeSoundHandler$Sound</B> class
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class FadingSound extends MovingSound
{
    protected int fadeSpeed;
    protected int fadeInTicks;

    public FadingSound(@Nonnull SoundEvent soundIn, @Nonnull SoundCategory category) {
        super(soundIn, category);
        repeat = true;
    }

    @Override
    public void update() {
        if(fadeInTicks < 0) {
            donePlaying = true;
            repeat = false;
        }

        fadeInTicks += fadeSpeed;
        volume = MathHelper.clamp(fadeInTicks / 40f, 0, 1);
    }

    public void fadeOut() {
        fadeInTicks = Math.min(fadeInTicks, 40);
        fadeSpeed = -1;
    }

    public void fadeIn() {
        fadeInTicks = Math.max(0, fadeInTicks);
        fadeSpeed = 1;
    }
}
