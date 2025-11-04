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
