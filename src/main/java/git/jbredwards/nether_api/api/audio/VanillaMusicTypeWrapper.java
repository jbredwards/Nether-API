package git.jbredwards.nether_api.api.audio;

import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A client-side IMusicType implementation that wraps around Vanilla's MusicType enum.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class VanillaMusicTypeWrapper implements IMusicType
{
    @Nonnull
    public final MusicType musicType;
    public VanillaMusicTypeWrapper(@Nonnull MusicType musicTypeIn) {
        musicType = Objects.requireNonNull(musicTypeIn, "MusicType cannot be null!");
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() { return musicType.getMusicLocation(); }

    @Override
    public int getMinDelay() { return musicType.getMinDelay(); }

    @Override
    public int getMaxDelay() { return musicType.getMaxDelay(); }
}
