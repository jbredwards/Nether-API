/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.client.audio.TheEndMusicHandler;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenEndGateway;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public class WorldProviderTheEnd extends WorldProviderEnd implements IAmbienceWorldProvider, IFogWorldProvider
{
    // Non-hardcoded so mod devs can customize these!
    @Nonnull public static ExitPortal EXIT_PORTAL = WorldGenEndPodium::new;
    @Nonnull public static WorldGenerator END_GATEWAY = new WorldGenEndGateway();
    @Nonnull public static WorldGenSpikes END_PILLAR = new WorldGenSpikes(); // used via asm
    public static boolean forceExtraEndFog = false;

    @Override
    public void init() {
        biomeProvider = new BiomeProviderTheEnd(world);

        // setup serverside handlers
        if(world instanceof WorldServer) {
            final NBTTagCompound fightData = world.getWorldInfo().getDimensionData(getDimension()).getCompoundTag("DragonFight");
            dragonFightManager = new DragonFightManager((WorldServer)world, fightData) {
                @Override
                public void generateGateway(@Nonnull BlockPos pos) {
                    world.playEvent(Constants.WorldEvents.GATEWAY_SPAWN_EFFECTS, pos, 0);
                    END_GATEWAY.generate(world, createSeedRandom(pos), pos);
                }

                @Override
                public void generatePortal(boolean active) {
                    if(exitPortalLocation == null) {
                        exitPortalLocation = world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION).down();
                        // ensure the exit portal pos is on the ground, and not on top of any previously existing portal
                        while(world.getBlockState(exitPortalLocation).getBlock() == Blocks.BEDROCK && exitPortalLocation.getY() > world.getSeaLevel())
                            exitPortalLocation = exitPortalLocation.down();
                    }

                    EXIT_PORTAL.create(active).generate(world, createSeedRandom(exitPortalLocation), exitPortalLocation);
                }
            };
        }

        // setup clientside handlers
        // TODO else setSkyRenderer(new EndSkyRenderHandler());
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorTheEnd(world, world.getWorldInfo().isMapFeaturesEnabled(), (BiomeProviderTheEnd)biomeProvider, getSpawnCoordinate());
    }

    @Nullable
    @Override
    public IDarkSoundAmbience getDarkAmbienceSound(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(final int x, final int z) {
        if(forceExtraEndFog) return true;
        @Nonnull final Biome biome = world.getBiome(new BlockPos(x, 0, z));
        return biome instanceof IEndBiome && ((IEndBiome)biome).hasExtraXZFog(world, x, z);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(final float celestialAngle, final float partialTicks) {
        return getFogColor(world, celestialAngle, partialTicks, 0.09411766, 0.07529412, 0.09411766, NetherAPIFogColorEvent.End::new);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getDefaultFogColor(@Nonnull final Biome biome, final float celestialAngle, final float partialTicks, final double defaultR, final double defaultG, final double defaultB) {
        return biome instanceof IEndBiome ? ((IEndBiome)biome).getFogColor(celestialAngle, partialTicks) : new Vec3d(defaultR, defaultG, defaultB);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public MusicTicker.MusicType getMusicType() { return TheEndMusicHandler.getMusicType(); }

    /**
     * @return a new random whose seed is based on a combination of the world seed + chunk pos.
     */
    @Nonnull
    public Random createSeedRandom(@Nonnull final BlockPos pos) {
        final Random rand = new Random(world.getSeed());
        rand.setSeed(((rand.nextLong() >> 2 + 1) * (pos.getX() >> 4) + (rand.nextLong() >> 2 + 1) * (pos.getZ() >> 4)) ^ world.getSeed());
        return rand;
    }

    /**
     * Allows mod devs to create their own end exit portals!
     */
    @FunctionalInterface
    public interface ExitPortal { WorldGenerator create(boolean activated); }
}
