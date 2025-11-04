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

package git.jbredwards.nether_api.mod.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.entity.EntityPlayerSP;
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
    @Nonnull
    protected final EntityPlayerSP player;
    protected int fadeSpeed;
    protected int fadeInTicks;

    public FadingSound(@Nonnull EntityPlayerSP playerIn, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category) {
        super(soundIn, category);
        player = playerIn;
        repeat = true;
    }

    @Override
    public void update() {
        if(fadeInTicks < 0 || player.isDead || !player.isAddedToWorld()) {
            donePlaying = true;
            repeat = false;
        }

        xPosF = (float)player.posX;
        yPosF = (float)player.posY;
        zPosF = (float)player.posZ;
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
