package git.jbredwards.nether_api.api.audio;

import net.minecraft.util.SoundEvent;

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
     * @return the SoundEvent that gets played.
     */
    @Nonnull
    SoundEvent getSoundEvent();

    /**
     * @return the minimum delay between playing music.
     */
    int getMinDelay();

    /**
     * @return the maximum delay between playing music.
     */
    int getMaxDelay();
}
