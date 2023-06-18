package git.jbredwards.nether_api.api.audio;

/**
 * Allows biomes to have their own darkness ambient sound (cave sounds).
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IDarkSoundAmbience extends ISoundAmbience
{
    /**
     * @return
     */
    int getLightSearchRadius();

    /**
     * @return
     */
    double getSoundOffset();
}
