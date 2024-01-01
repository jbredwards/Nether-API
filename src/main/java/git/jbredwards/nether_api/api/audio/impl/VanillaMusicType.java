/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.audio.impl;

import git.jbredwards.nether_api.api.audio.IMusicType;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The default implementation of {@link IMusicType}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public class VanillaMusicType implements IMusicType
{
    @Nonnull
    @SideOnly(Side.CLIENT)
    public final MusicType musicType;

    @SideOnly(Side.CLIENT)
    public VanillaMusicType(@Nullable MusicType musicTypeIn) {
        musicType = Objects.requireNonNull(musicTypeIn, "MusicType cannot be null!");
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public MusicType getMusicType() { return musicType; }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean replacesCurrentMusic(@Nonnull MusicType currentlyPlaying) { return false; }
}
