/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.client.audio.NetherMusicHandler;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNether;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorNether;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class WorldProviderNether extends WorldProviderHell implements IAmbienceWorldProvider, IFogWorldProvider
{
    public static boolean FORCE_NETHER_FOG = false;

    @Override
    public void init() {
        if(NetherAPIRegistry.NETHER.isEmpty()) MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.Nether(NetherAPIRegistry.NETHER, world));
        biomeProvider = new BiomeProviderNether(world.getWorldType(), world.getSeed());
        doesWaterVaporize = true;
        nether = true;
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorNether(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed());
    }

    @Nullable
    @Override
    public IDarkSoundAmbience getDarkAmbienceSound(@Nonnull Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : null;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return getFogColor(world, celestialAngle, partialTicks, 0.2, 0.03, 0.03, NetherAPIFogColorEvent.Nether::new);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getDefaultFogColor(@Nonnull Biome biome, float celestialAngle, float partialTicks, double defaultR, double defaultG, double defaultB) {
        return biome instanceof INetherBiome ? ((INetherBiome)biome).getFogColor(celestialAngle, partialTicks) : new Vec3d(defaultR, defaultG, defaultB);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) { return null; }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public MusicTicker.MusicType getMusicType() { return NetherMusicHandler.getMusicType(); }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z) {
        return FORCE_NETHER_FOG || !NetherAPI.isNetherExLoaded || NetherExHandler.doesXZShowFog(); //preserve NetherEx's fog settings if that mod is present
    }
}
