/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
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

            if(nbt.hasKey(NetherAPI.MODID + ":allowRespawn", Constants.NBT.TAG_ANY_NUMERIC)) allowRespawn = nbt.getBoolean(NetherAPI.MODID + ":allowRespawn");
            else nbt.setBoolean(NetherAPI.MODID + ":allowRespawn", allowRespawn = (ALLOW_RESPAWN != null ? ALLOW_RESPAWN : NetherAPIConfig.worldForSpawn.test(world)));

            if(nbt.hasKey(NetherAPI.MODID + ":allowSleep", Constants.NBT.TAG_ANY_NUMERIC)) allowRespawn = nbt.getBoolean(NetherAPI.MODID + ":allowSleep");
            else nbt.setBoolean(NetherAPI.MODID + ":allowSleep", allowSleep = (ALLOW_SLEEP != null ? ALLOW_SLEEP : NetherAPIConfig.worldForSpawn.test(world)));
        }
    }

    @Override
    public void onWorldSave() {
        @Nonnull final NBTTagCompound nbt = new NBTTagCompound();
        if(dragonFightManager != null) nbt.setTag("DragonFight", dragonFightManager.getCompound());

        nbt.setBoolean(NetherAPI.MODID + ":allowRespawn", allowRespawn);
        nbt.setBoolean(NetherAPI.MODID + ":allowSleep", allowSleep);
        world.getWorldInfo().setDimensionData(getDimension(), nbt);
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

                    spawnNewGateway();
                    generatePortal(false);

                    dragonKilled = true;
                    previouslyKilled = false;
                    scanForLegacyFight = false;
                }
            }

            super.tick();
        }

        @Override
        public void generateGateway(@Nonnull BlockPos pos) {
            if(!scanForLegacyFight) world.playEvent(Constants.WorldEvents.GATEWAY_SPAWN_EFFECTS, pos, 0);
            END_GATEWAY.generate(world, createSeedRandom(pos), pos);
        }

        @Override
        public void generatePortal(boolean active) {
            if(exitPortalLocation == null) {
                exitPortalLocation = world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION).down();
                final int exitPortalMinY = Math.max(world.getSeaLevel(), 2);

                // ensure the exit portal pos is on the ground, and not on top of any previously existing portal
                @Nonnull final Chunk chunk = world.getChunk(exitPortalLocation);
                while(exitPortalLocation.getY() > exitPortalMinY && chunk.getBlockState(exitPortalLocation).getBlock() == Blocks.BEDROCK)
                    exitPortalLocation = exitPortalLocation.down();

                // prevent portals from spawning into the void
                if(exitPortalLocation.getY() < exitPortalMinY) exitPortalLocation = new BlockPos(exitPortalLocation.getX(), exitPortalMinY, exitPortalLocation.getZ());
            }

            EXIT_PORTAL.create(active && (initialDragon || initialDragonKilled)).generate(world, createSeedRandom(exitPortalLocation), exitPortalLocation);
        }

        @Override
        public void processDragonDeath(@Nonnull final EntityDragon dragon) {
            if(dragon.getUniqueID().equals(dragonUniqueId)) initialDragonKilled = true;
            super.processDragonDeath(dragon);
        }
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
    public MusicTicker.MusicType getMusicType() {
        return TheEndMusicHandler.getMusicType();
    }

    // ----------------------------
    // remove hardcoded spawn logic
    // ----------------------------

    @Nullable public static Boolean ALLOW_RESPAWN = null;
    @Nullable public static Boolean ALLOW_SLEEP = null;

    protected boolean allowRespawn = false;
    protected boolean allowSleep = false;

    @Override
    public boolean canCoordinateBeSpawn(final int x, final int z) {
        if(world.getHeight(x, z) == 0) return false;

        @Nonnull final BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        if(world.getBiome(pos).ignorePlayerSpawnSuitability()) return true;

        @Nonnull final IBlockState state = world.getBlockState(pos);
        return state.getBlock() != Blocks.OBSIDIAN && !state.getMaterial().isLiquid() && world.isAirBlock(pos.up(2))
                && (world.isRemote || state.canEntitySpawn(FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0))));
    }

    @Override
    public boolean canDropChunk(final int x, final int z) {
        return !canRespawnHere() || !world.isSpawnChunk(x, z);
    }

    @Nonnull
    @Override
    public WorldSleepResult canSleepAt(@Nonnull final EntityPlayer player, @Nonnull final BlockPos pos) {
        return allowSleep ? WorldSleepResult.ALLOW : WorldSleepResult.BED_EXPLODES;
    }

    @Override
    public boolean canRespawnHere() {
        return allowRespawn || super.canRespawnHere();
    }

    @Override
    public int getRespawnDimension(@Nonnull final EntityPlayerMP player) {
        if(player.hasSpawnDimension()) return player.getSpawnDimension();
        else return canRespawnHere() ? getDimension() : 0;
    }

    @Nonnull
    @Override
    public BlockPos getRandomizedSpawnPoint() {
        @Nonnull BlockPos ret = Objects.requireNonNull(getSpawnCoordinate());
        OBSIDIAN_PLATFORM.generate(world, new Random(world.getSeed()), ret);

        int spawnFuzz = 50;
        final int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if(border < spawnFuzz) spawnFuzz = border;

        if(border != 0) {
            if(spawnFuzz < 2) spawnFuzz = 2;
            final int spawnFuzzHalf = spawnFuzz >> 1;
            final int spawnAttempts = 1000; // Same # of spawn attempts as Overworld.

            for(int i = 0; i < spawnAttempts; i++) {
                final int x = ret.getX() + spawnFuzzHalf - world.rand.nextInt(spawnFuzz);
                final int z = ret.getZ() + spawnFuzzHalf - world.rand.nextInt(spawnFuzz);

                if(canCoordinateBeSpawn(x, z)) return world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            }
        }

        return world.getTopSolidOrLiquidBlock(ret);
    }
}
