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
@SideOnly(Side.CLIENT)
public class VanillaMusicType implements IMusicType
{
    @Nonnull
    public final MusicType musicType;
    public VanillaMusicType(@Nullable MusicType musicTypeIn) {
        musicType = Objects.requireNonNull(musicTypeIn, "MusicType cannot be null!");
    }

    @Nonnull
    @Override
    public MusicType getMusicType() { return musicType; }

    @Override
    public boolean replacesCurrentMusic(@Nonnull MusicType currentlyPlaying) { return false; }
}
