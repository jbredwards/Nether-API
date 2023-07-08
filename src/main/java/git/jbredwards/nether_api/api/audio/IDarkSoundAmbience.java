/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

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
     * @return the radius that can be scanned for light values.
     */
    int getLightSearchRadius();

    /**
     * @return the general distance away from the player where the sound will be played.
     */
    double getSoundOffset();
}
