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

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.client.audio.TheEndMusicHandler;
import git.jbredwards.nether_api.mod.common.compat.voidislandcontrol.VoidIslandControlHandler;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenEndGateway;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
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
    @Nonnull public static WorldGenerator OBSIDIAN_PLATFORM = new WorldGenerator() {
        @Override
        public boolean generate(@Nonnull final World worldIn, @Nonnull final Random rand, @Nonnull final BlockPos position) {
            for(int x = -2; x <= 2; x++) for(int z = -2; z <= 2; z++) {
                worldIn.setBlockState(position.add(x, -2, z), Blocks.OBSIDIAN.getDefaultState());
                for(int y = -1; y < 2; y++) worldIn.setBlockState(position.add(x, y, z), Blocks.AIR.getDefaultState());
            }

            return true;
        }
    };

    @Override
    public void setDimension(final int dim) {
        if(DimensionManager.getProviderType(dim).clazz != getClass()) throw new IllegalArgumentException("Invalid dimension ID: " + dim);
        super.setDimension(dim);
    }

    @Override
    public void init() {
        biomeProvider = new BiomeProviderTheEnd(world);

        // setup serverside handlers
        if(world instanceof WorldServer) {
            @Nonnull final NBTTagCompound nbt = world.getWorldInfo().getDimensionData(getDimension());
            dragonFightManager = new FightManager((WorldServer)world, nbt.getCompoundTag("DragonFight"));
        }
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorTheEnd(world, VoidIslandControlHandler.isMapFeaturesEnabled(world), (BiomeProviderTheEnd)biomeProvider, getSpawnCoordinate());
    }

    @Nonnull
    @Override
    public BlockPos getSpawnCoordinate() {
        return VoidIslandControlHandler.getEndSpawnPos(super.getSpawnCoordinate());
    }

    /**
     * @return a new random whose seed is based on a combination of the world seed + chunk pos.
     */
    @Nonnull
    public Random createSeedRandom(@Nonnull final BlockPos pos) {
        return NetherGenerationUtils.createSeedRandom(world.getSeed(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Allows mod devs to create their own end exit portals!
     */
    @FunctionalInterface
    public interface ExitPortal { WorldGenerator create(boolean activated); }
    protected class FightManager extends DragonFightManager
    {
        public boolean initialDragon = NetherAPIConfig.initialDragon, initialDragonKilled = false;
        public FightManager(@Nonnull final WorldServer worldIn, @Nonnull final NBTTagCompound compound) {
            super(worldIn, compound);
            if(compound.hasKey(NetherAPI.MODID + ":initialDragon", Constants.NBT.TAG_ANY_NUMERIC)) {
                initialDragon = compound.getBoolean(NetherAPI.MODID + ":initialDragon");
                initialDragonKilled = compound.getBoolean(NetherAPI.MODID + ":initialDragonKilled");
            }

            else if(!compound.isEmpty()) initialDragonKilled = dragonKilled; // For worlds that were upgraded from v1.3.0 or earlier.
        }

        @Nonnull
        @Override
        public NBTTagCompound getCompound() {
            @Nonnull final NBTTagCompound nbt = super.getCompound();
            nbt.setBoolean(NetherAPI.MODID + ":initialDragon", initialDragon);
            nbt.setBoolean(NetherAPI.MODID + ":initialDragonKilled", initialDragonKilled);
            return nbt;
        }

        @Override
        public void tick() {
            if(!initialDragon) {
                if(scanForLegacyFight) {
                    @Nonnull final List<EntityDragon> dragons = world.getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE);
                    if(!dragons.isEmpty()) dragons.get(0).setDead(); // For worlds that were upgraded from 1.8 or earlier.

                    generatePortal(true);
                    spawnNewGateway();

                    dragonKilled = true;
                    previouslyKilled = false;
                    scanForLegacyFight = false;
                }
            }

            super.tick();
        }

        @Override
        public void generateGateway(@Nonnull final BlockPos pos) {
            if(!scanForLegacyFight) world.playEvent(Constants.WorldEvents.GATEWAY_SPAWN_EFFECTS, pos, 0);
            END_GATEWAY.generate(world, createSeedRandom(pos), pos);
        }

        @Override
        public void generatePortal(final boolean active) {
            if(exitPortalLocation == null) {
                exitPortalLocation = world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION).down();
                final int exitPortalMinY = Math.max(world.getSeaLevel(), 2);

                // ensure the exit portal pos is on the ground, and not on top of any previously existing portal
                @Nonnull final Chunk chunk = world.getChunk(exitPortalLocation);
                while(chunk.getBlockState(exitPortalLocation).getBlock() == Blocks.BEDROCK) exitPortalLocation = exitPortalLocation.down();

                // prevent portals from spawning into the void
                if(exitPortalLocation.getY() < 2) exitPortalLocation = new BlockPos(exitPortalLocation.getX(), exitPortalMinY, exitPortalLocation.getZ());
            }

            EXIT_PORTAL.create(spawnPortalLit(active)).generate(world, createSeedRandom(exitPortalLocation), exitPortalLocation);
        }

        @Override
        public void processDragonDeath(@Nonnull final EntityDragon dragon) {
            if(dragon.getUniqueID().equals(dragonUniqueId)) initialDragonKilled = true;
            super.processDragonDeath(dragon);
        }

        protected boolean spawnPortalLit(final boolean active) {
            if(!active) return false;
            else if(initialDragonKilled) return true;
            else if(initialPortalLit != null) return initialPortalLit;
            else return PlayerSpawnLogic.getInitialSpawnDimension(null) != getDimension();
        }
    }

    @Nullable
    protected static Boolean initialPortalLit = null;
    public static void overrideInitialPortalLit(final boolean value) { initialPortalLit = value; }

    @Nonnull
    @Override
    public BlockPos getRandomizedSpawnPoint() {
        OBSIDIAN_PLATFORM.generate(world, new Random(world.getSeed()), getSpawnCoordinate());
        return super.getRandomizedSpawnPoint();
    }

    // --------------
    // biome ambience
    // --------------

    public static boolean forceExtraEndFog = false;

    @Nullable
    @Override
    public IDarkSoundAmbience getDarkAmbienceSound(@Nonnull final Biome biome) {
        return biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : null;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(final float celestialAngle, final float partialTicks) {
        return getFogColor(world, celestialAngle, partialTicks);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getBiomeFogColor(final float celestialAngle, final float partialTicks, @Nonnull final Biome biome) {
        return biome instanceof IEndBiome ? ((IEndBiome)biome).getFogColor(celestialAngle, partialTicks) : getDefaultFogColor(celestialAngle, partialTicks);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getDefaultFogColor(final float celestialAngle, final float partialTicks) {
        return new Vec3d(0.09411766, 0.07529412, 0.09411766);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public NetherAPIFogColorEvent createEvent(@Nonnull final Biome biomeIn, @Nonnull final World worldIn, final float celestialAngleIn, final float partialTicksIn) {
        return new NetherAPIFogColorEvent.End(biomeIn, worldIn, celestialAngleIn, partialTicksIn);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public MusicTicker.MusicType getMusicType() {
        return TheEndMusicHandler.getMusicType();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(final int x, final int z) {
        if(forceExtraEndFog) return true;
        @Nonnull final Biome biome = world.getBiome(new BlockPos(x, 0, z));
        return biome instanceof IEndBiome && ((IEndBiome)biome).hasExtraXZFog(world, x, z);
    }
}
