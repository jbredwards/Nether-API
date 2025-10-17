/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.client.audio.NetherMusicHandler;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.compat.voidislandcontrol.VoidIslandControlHandler;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNether;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorNether;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayerFactory;
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
    @Override
    public void setDimension(final int dim) {
        if(DimensionManager.getProviderType(dim).clazz != getClass()) throw new IllegalArgumentException("Invalid dimension ID: " + dim);
        super.setDimension(dim);
    }

    @Override
    public void init() {
        biomeProvider = new BiomeProviderNether(world);
        doesWaterVaporize = true;
        nether = true;

        // setup serverside handlers
        if(world instanceof WorldServer) {
            @Nonnull final NBTTagCompound nbt = world.getWorldInfo().getDimensionData(getDimension());

            if(nbt.hasKey(NetherAPI.MODID + ":allowRespawn", Constants.NBT.TAG_ANY_NUMERIC)) allowRespawn = nbt.getBoolean(NetherAPI.MODID + ":allowRespawn");
            else nbt.setBoolean(NetherAPI.MODID + ":allowRespawn", allowRespawn = (ALLOW_RESPAWN != null ? ALLOW_RESPAWN : NetherAPIConfig.worldForSpawn.test(world)));

            if(nbt.hasKey(NetherAPI.MODID + ":allowSleep", Constants.NBT.TAG_ANY_NUMERIC)) allowRespawn = nbt.getBoolean(NetherAPI.MODID + ":allowSleep");
            else nbt.setBoolean(NetherAPI.MODID + ":allowSleep", allowSleep = (ALLOW_SLEEP != null ? ALLOW_SLEEP : NetherAPIConfig.worldForSpawn.test(world)));
        }
    }

    @Override
    public void onWorldSave() {
        @Nonnull final NBTTagCompound nbt = new NBTTagCompound();

        nbt.setBoolean(NetherAPI.MODID + ":allowRespawn", allowRespawn);
        nbt.setBoolean(NetherAPI.MODID + ":allowSleep", allowSleep);
        world.getWorldInfo().setDimensionData(getDimension(), nbt);
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorNether(world, VoidIslandControlHandler.isMapFeaturesEnabled(world), world.getSeed());
    }

    @Override
    public int getActualHeight() { return NetherAPIConfig.tallNether ? 256 : super.getActualHeight(); }

    // --------------
    // biome ambience
    // --------------

    public static boolean FORCE_NETHER_FOG = false;

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
    public MusicTicker.MusicType getMusicType() {
        return NetherMusicHandler.getMusicType();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(int x, int z) {
        return FORCE_NETHER_FOG || !NetherAPI.isNetherExLoaded || NetherExHandler.doesXZShowFog(); // preserve NetherEx's fog settings if that mod is present
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) { return null; }

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

        @Nonnull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, world.getSeaLevel(), z);
        @Nonnull final Chunk chunk = world.getChunk(pos);
        while(!isAirBlock(chunk, pos.move(EnumFacing.UP)));

        if(pos.move(EnumFacing.DOWN).getY() == getActualHeight() - 1) return false; // Never spawn players on the nether roof.
        else if(world.getBiome(pos).ignorePlayerSpawnSuitability()) return true;

        @Nonnull final IBlockState state = chunk.getBlockState(pos);
        return !state.getMaterial().isLiquid() && isAirBlock(chunk, pos.move(EnumFacing.UP, 2))
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
        @Nonnull BlockPos ret = world.getSpawnPoint();

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

                if(canCoordinateBeSpawn(x, z)) {
                    @Nonnull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, world.getSeaLevel(), z);
                    @Nonnull final Chunk chunk = world.getChunk(pos);

                    while(!isAirBlock(chunk, pos.move(EnumFacing.UP)));
                    return pos.move(EnumFacing.DOWN).toImmutable();
                }
            }
        }

        return ret;
    }

    private static boolean isAirBlock(@Nonnull final Chunk chunk, @Nonnull final BlockPos pos) {
        @Nonnull final IBlockState state = chunk.getBlockState(pos);
        return !state.getBlock().isAir(state, chunk.getWorld(), pos);
    }
}
