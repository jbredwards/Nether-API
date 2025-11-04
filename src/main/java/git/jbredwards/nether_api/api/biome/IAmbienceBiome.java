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
