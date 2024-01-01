/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Biomes that implement this have access to many new tools for improving the passive biome ambience.
 * This interface is not exclusive to dimensional biomes!
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IAmbienceBiome
{
    /**
     * Air in this biome spawns these once per random block render tick (same as torch particles).
     * One gets picked from random to be played. If the factory returns null, a different factory is randomly chosen.
     * @return the possible ambient particle factories
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    default IParticleFactory[] getAmbientParticles() { return null; }

    /**
     * @return the ambient sound that continuously plays while in this biome.
     */
    @Nullable
    default SoundEvent getAmbientSound() { return null; }

    /**
     * @return the ambient sound that randomly plays while in this biome.
     */
    @Nullable
    default ISoundAmbience getRandomAmbientSound() { return null; }

    /**
     * @return the ambient sound that randomly plays in dark areas (cave sounds).
     */
    @Nullable
    default IDarkSoundAmbience getDarkAmbienceSound() { return DarkSoundAmbience.DEFAULT_CAVE; }
}
