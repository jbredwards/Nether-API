/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Allows WorldProviders that implement this to call their own biome ambience code instead.
 *
 * @since 1.1.1
 * @author jbred
 *
 */
public interface IAmbienceWorldProvider
{
    /**
     * Utility function that tries getting ambience from the provider, then the biome, then the fallback.
     * @since 1.3.0
     */
    @Nullable
    static <T> T getAmbienceOrFallback(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Biome biome, @Nonnull Class<T> ambienceType, @Nonnull BiFunction<IAmbienceWorldProvider, Biome, T> pFun, @Nonnull Function<IAmbienceBiome, T> bFun, @Nullable T fallback) {
        final BiomeAmbienceEvent<T> event = new BiomeAmbienceEvent<>(ambienceType, biome, world, pos);
        if(MinecraftForge.EVENT_BUS.post(event)) return event.ambience;

        return world.provider instanceof IAmbienceWorldProvider
                ? pFun.apply((IAmbienceWorldProvider)world.provider, biome)
                : biome instanceof IAmbienceBiome ? bFun.apply((IAmbienceBiome)biome) : fallback;
    }

    /**
     * Air in the provided biome spawns these once per random block render tick (same as torch particles).
     * One gets picked from random to be played. If the factory returns null, a different factory is randomly chosen.
     * @return the possible ambient particle factories
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    default IParticleFactory[] getAmbientParticles(@Nonnull Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getAmbientParticles() : null;
    }

    /**
     * @return the ambient sound that continuously plays while in the provided biome.
     */
    @Nullable
    default SoundEvent getAmbientSound(@Nonnull Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getAmbientSound() : null;
    }

    /**
     * @return the ambient sound that randomly plays while in the provided biome.
     */
    @Nullable
    default ISoundAmbience getRandomAmbientSound(@Nonnull Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getRandomAmbientSound() : null;
    }

    /**
     * @return the ambient sound that randomly plays in dark areas (cave sounds).
     */
    @Nullable
    default IDarkSoundAmbience getDarkAmbienceSound(@Nonnull Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : DarkSoundAmbience.DEFAULT_CAVE;
    }
}
