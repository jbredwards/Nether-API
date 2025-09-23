/*
 * Copyright (c) 2023-2025. jbredwards
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
    private final boolean replacesCurrent;

    @SideOnly(Side.CLIENT)
    public VanillaMusicType(@Nullable MusicType musicTypeIn) {
        this(musicTypeIn, false);
    }

    // Since 1.4.0
    @SideOnly(Side.CLIENT)
    public VanillaMusicType(@Nullable MusicType musicTypeIn, boolean replacesCurrentIn) {
        musicType = Objects.requireNonNull(musicTypeIn, "MusicType cannot be null!");
        replacesCurrent = replacesCurrentIn;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public MusicType getMusicType() { return musicType; }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean replacesCurrentMusic(@Nonnull MusicType currentlyPlaying) { return replacesCurrent; }
}
