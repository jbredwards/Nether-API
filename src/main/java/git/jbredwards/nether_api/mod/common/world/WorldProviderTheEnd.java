/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.client.audio.TheEndMusicHandler;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.end.DragonFightManager;
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
public class WorldProviderTheEnd extends WorldProviderEnd implements IAmbienceWorldProvider, IFogWorldProvider
{
    @Override
    public void init() {
        if(NetherAPIRegistry.THE_END.isEmpty()) MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.End(NetherAPIRegistry.THE_END, world));
        biomeProvider = new BiomeProviderTheEnd(world.getWorldType(), world.getSeed());
        if(world instanceof WorldServer) dragonFightManager = new DragonFightManager((WorldServer)world, world.getWorldInfo().getDimensionData(getDimension()).getCompoundTag("DragonFight"));
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorTheEnd(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed(), getSpawnCoordinate());
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
        return getFogColor(world, celestialAngle, partialTicks, 0, 0, 0, NetherAPIFogColorEvent.End::new);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getDefaultFogColor(@Nonnull Biome biome, float celestialAngle, float partialTicks, double defaultR, double defaultG, double defaultB) {
        return biome instanceof IEndBiome ? ((IEndBiome)biome).getFogColor(celestialAngle, partialTicks) : new Vec3d(defaultR, defaultG, defaultB);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public MusicTicker.MusicType getMusicType() { return TheEndMusicHandler.getMusicType(); }

    // =============================================
    // ALLOW THE END TO HAVE CUSTOM SKY & FOG COLORS
    // =============================================

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored() { return true; }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z) { return true; }
}
