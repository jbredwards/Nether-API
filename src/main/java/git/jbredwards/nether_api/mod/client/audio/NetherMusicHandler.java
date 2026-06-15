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

import git.jbredwards.nether_api.api.audio.IMusicBiome;
import git.jbredwards.nether_api.api.audio.IMusicType;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Deprecated
@SideOnly(Side.CLIENT)
public final class NetherMusicHandler
{
    @Nonnull
    private static final IMusicBiome DEFAULT_MUSIC = IMusicBiome.of(
            IMusicType.vanilla(MusicTicker.MusicType.NETHER)
    );

    @Nonnull
    public static MusicTicker.MusicType getMusicType() {
        return BiomeMusicHandler.get(DimensionType.NETHER, DEFAULT_MUSIC);
    }
}
