/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.common.config.ambience;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.event.BiomeAmbienceEvent;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = NetherAPI.MODID)
final class AmbienceConfigHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void clientEndFog(@Nonnull final NetherAPIFogColorEvent.End event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.endFogColor != null) {
            event.fogR = cfg.endFogColor[0];
            event.fogG = cfg.endFogColor[1];
            event.fogB = cfg.endFogColor[2];
            event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void clientNetherFog(@Nonnull final NetherAPIFogColorEvent.Nether event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.netherFogColor != null) {
            event.fogR = cfg.netherFogColor[0];
            event.fogG = cfg.netherFogColor[1];
            event.fogB = cfg.netherFogColor[2];
            event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void clientParticles(@Nonnull final BiomeAmbienceEvent<IParticleFactory[]> event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.ambience != null) {
            @Nullable final IParticleFactory[] ambience = cfg.ambience.getAmbientParticles();
            if(ambience != null) {
                event.ambience = ambience;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void darkSound(@Nonnull final BiomeAmbienceEvent<IDarkSoundAmbience> event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.ambience != null) {
            @Nullable final IDarkSoundAmbience ambience = cfg.ambience.getDarkAmbienceSound();
            if(ambience != null) {
                event.ambience = ambience;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void passiveSound(@Nonnull final BiomeAmbienceEvent<SoundEvent> event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.ambience != null) {
            @Nullable final SoundEvent ambience = cfg.ambience.getAmbientSound();
            if(ambience != null) {
                event.ambience = ambience;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void randomSound(@Nonnull final BiomeAmbienceEvent<ISoundAmbience> event) {
        @Nullable final AmbienceConfig.Value cfg = AmbienceConfig.get(event.biome);
        if(cfg != null && cfg.ambience != null) {
            @Nullable final ISoundAmbience ambience = cfg.ambience.getRandomAmbientSound();
            if(ambience != null) {
                event.ambience = ambience;
                event.setCanceled(true);
            }
        }
    }
}
