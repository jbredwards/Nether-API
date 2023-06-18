package git.jbredwards.nether_api.api.audio;

import net.minecraft.client.audio.MusicTicker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Allows nether biomes to have their own music, called by
 * {@link git.jbredwards.nether_api.api.biome.INetherBiome#getMusicType INetherBiome::getMusicType}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IMusicType
{
    /**
     * @return the MusicType that gets played.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    MusicTicker.MusicType getMusicType();

    /**
     * @return whether this should replace the currently playing music.
     */
    @SideOnly(Side.CLIENT)
    boolean replacesCurrentMusic(@Nonnull MusicTicker.MusicType currentlyPlaying);
}
