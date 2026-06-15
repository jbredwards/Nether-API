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

import git.jbredwards.nether_api.api.audio.IMusicType;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The default implementation of {@link IMusicType}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public class VanillaMusicType implements IMusicType
{
    @Nonnull
    @SideOnly(Side.CLIENT)
    public final MusicType musicType;
    private final boolean replacesCurrent, isLocal;

    @ApiStatus.AvailableSince("1.0.0")
    @SideOnly(Side.CLIENT)
    public VanillaMusicType(@Nullable MusicType musicTypeIn) {
        this(musicTypeIn, false, false);
    }

    @ApiStatus.AvailableSince("1.4.0")
    @SideOnly(Side.CLIENT)
    public VanillaMusicType(@Nullable MusicType musicTypeIn, boolean replacesCurrentIn, boolean isLocalIn) {
        musicType = Objects.requireNonNull(musicTypeIn, "MusicType cannot be null!");
        replacesCurrent = replacesCurrentIn;
        isLocal = isLocalIn;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public MusicType getMusicType() {
        return musicType;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean replacesCurrentMusic(@Nonnull MusicType currentlyPlaying) {
        return replacesCurrent;
    }

    @Override
    public boolean isBiomeLocal() {
        return isLocal;
    }
}
