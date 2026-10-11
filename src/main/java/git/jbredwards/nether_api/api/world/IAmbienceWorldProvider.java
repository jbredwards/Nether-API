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

package git.jbredwards.nether_api.api.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.event.BiomeAmbienceEvent;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Allows WorldProviders that implement this to call their own biome ambience code instead.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.1.1")
public interface IAmbienceWorldProvider
{
    /**
     * Utility function that tries getting ambience from the provider, then the biome, then the fallback.
     * @throws NullPointerException If any parameters (except fallback) are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.3.0")
    @ApiStatus.Obsolete(since = "1.5.0")
    @Nullable
    static <T> T getAmbienceOrFallback(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final Biome biome, @Nonnull final Class<T> ambienceType, @Nonnull final BiFunction<IAmbienceWorldProvider, Biome, T> pFun, @Nonnull final Function<IAmbienceBiome, T> bFun, @Nullable final T fallback) {
        @Nonnull final BiomeAmbienceEvent<T> event = new BiomeAmbienceEvent<>(ambienceType, biome, world, pos);
        if(MinecraftForge.EVENT_BUS.post(event)) return event.ambience;

        else if(world.provider instanceof IAmbienceWorldProvider) return pFun.apply((IAmbienceWorldProvider)world.provider, biome);
        else if(biome instanceof IAmbienceBiome) return bFun.apply((IAmbienceBiome)biome);

        else return fallback;
    }

    /**
     * Air in the provided biome spawns these once per random block render tick (same as torch particles).
     * One gets picked from random to be played. If the factory returns null, a different factory is randomly chosen.
     * @return The possible ambient particle factories.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.1.1")
    @ApiStatus.OverrideOnly
    @Nullable
    @SideOnly(Side.CLIENT)
    default IParticleFactory[] getAmbientParticles(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getAmbientParticles() : null;
    }

    /**
     * @return The possible ambient particle factories.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    @SideOnly(Side.CLIENT)
    static IParticleFactory[] getAmbientParticles(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final Biome biome) {
        return IAmbienceWorldProvider.getAmbienceOrFallback(world, pos, biome, IParticleFactory[].class, IAmbienceWorldProvider::getAmbientParticles, IAmbienceBiome::getAmbientParticles, null);
    }

    /**
     * @return The ambient sound that continuously plays while in the provided biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.1.1")
    @ApiStatus.OverrideOnly
    @Nullable
    default SoundEvent getAmbientSound(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getAmbientSound() : null;
    }

    /**
     * @return The ambient sound that continuously plays while in the provided biome.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    static SoundEvent getAmbientSound(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final Biome biome) {
        return IAmbienceWorldProvider.getAmbienceOrFallback(world, pos, biome, SoundEvent.class, IAmbienceWorldProvider::getAmbientSound, IAmbienceBiome::getAmbientSound, null);
    }

    /**
     * @return The ambient sound that randomly plays while in the provided biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.1.1")
    @ApiStatus.OverrideOnly
    @Nullable
    default ISoundAmbience getRandomAmbientSound(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getRandomAmbientSound() : null;
    }

    /**
     * @return The ambient sound that randomly plays while in the provided biome.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    static ISoundAmbience getRandomAmbientSound(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final Biome biome) {
        return IAmbienceWorldProvider.getAmbienceOrFallback(world, pos, biome, ISoundAmbience.class, IAmbienceWorldProvider::getRandomAmbientSound, IAmbienceBiome::getRandomAmbientSound, null);
    }

    /**
     * @return The ambient sound that randomly plays in dark areas (cave sounds).
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.1.1")
    @ApiStatus.OverrideOnly
    @Nullable
    default IDarkSoundAmbience getDarkAmbienceSound(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : DarkSoundAmbience.DEFAULT_CAVE;
    }

    /**
     * @return The ambient sound that randomly plays in dark areas (cave sounds).
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    static IDarkSoundAmbience getDarkAmbienceSound(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final Biome biome) {
        return IAmbienceWorldProvider.getAmbienceOrFallback(world, pos, biome, IDarkSoundAmbience.class, IAmbienceWorldProvider::getDarkAmbienceSound, IAmbienceBiome::getDarkAmbienceSound, DarkSoundAmbience.DEFAULT_CAVE);
    }
}
