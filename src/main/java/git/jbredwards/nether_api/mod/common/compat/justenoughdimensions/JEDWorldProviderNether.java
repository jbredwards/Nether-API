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

package git.jbredwards.nether_api.mod.common.compat.justenoughdimensions;

import fi.dy.masa.justenoughdimensions.util.ClientUtils;
import fi.dy.masa.justenoughdimensions.util.world.VoidTeleport;
import fi.dy.masa.justenoughdimensions.util.world.WorldInfoUtils;
import fi.dy.masa.justenoughdimensions.util.world.WorldUtils;
import fi.dy.masa.justenoughdimensions.world.IWorldProviderJED;
import fi.dy.masa.justenoughdimensions.world.JEDWorldProperties;
import fi.dy.masa.justenoughdimensions.world.WorldProviderJED;
import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public final class JEDWorldProviderNether extends WorldProviderNether implements IWorldProviderJED
{
    @Nullable
    private VoidTeleport.VoidTeleportData skyTeleport, voidTeleport;
    private JEDWorldProperties properties;

    private int teleportCounter;
    private boolean worldInfoSet, shouldSkipSpawnSearch;

    @Override
    public boolean getWorldInfoHasBeenSet() { return worldInfoSet; }

    @Override
    public boolean getShouldSkipSpawnSearch() { return shouldSkipSpawnSearch; }

    @Override
    public void setDimension(final int dim) {
        super.setDimension(dim);

        properties = JEDWorldProperties.getOrCreateProperties(dim);
        if(world != null && !getWorldInfoHasBeenSet()) {
            @Nonnull final BlockPos oldSpawnPoint = getSpawnPoint();

            WorldInfoUtils.loadAndSetCustomWorldInfo(world);
            worldInfoSet = true;

            shouldSkipSpawnSearch = !oldSpawnPoint.equals(getSpawnPoint());
            if(properties.getHasSkyLight() != null) hasSkyLight = properties.getHasSkyLight();

            skyTeleport = VoidTeleport.VoidTeleportData.fromJson(properties.getNestedObject("sky_teleport"), dim);
            voidTeleport = VoidTeleport.VoidTeleportData.fromJson(properties.getNestedObject("void_teleport"), dim);
        }
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        // WARNING:
        // Allowing JED to supply a custom chunk generator only exists for full compatibility with JED.
        // This is VERY likely to break Nether API reliant mods, so do this at your own risk!
        @Nullable final IChunkGenerator customGenerator = WorldProviderJED.createChunkGeneratorInstance(world, this);
        return customGenerator != null ? customGenerator : super.createChunkGenerator();
    }

    @Override
    public void onWorldUpdateEntities() {
        super.onWorldUpdateEntities();
        if(++teleportCounter >= properties.getVoidTeleportInterval()) {
            VoidTeleport.tryVoidTeleportEntities(world, voidTeleport, skyTeleport);
            teleportCounter = 0;
        }
    }

    // ============================
    // JEDWorldProperties Overrides
    // ============================

    @Override
    public void setJEDProperties(@Nonnull final JEDWorldProperties propertiesIn) {
        ClientUtils.setRenderersFrom(this, propertiesIn.getFullJEDProperties());
        properties = propertiesIn;
    }

    @Nonnull
    @Override
    public WorldSleepResult canSleepAt(@Nonnull final EntityPlayer player, @Nonnull final BlockPos pos) {
        @Nullable final WorldSleepResult val = properties.canSleepHere();
        return val != null ? val : super.canSleepAt(player, pos);
    }

    @Override
    public boolean canRespawnHere() {
        return WorldProviderJED.getBooleanOrDefault(properties.canRespawnHere(), super.canRespawnHere());
    }

    @Override
    public int getRespawnDimension(@Nonnull final EntityPlayerMP player) {
        if(properties.getRespawnDimension() != null) return properties.getRespawnDimension();
        else return canRespawnHere() ? getDimension() : 0;
    }

    @Nonnull
    @Override
    public float[] getLightBrightnessTable() {
        return properties.getCustomLightBrightnessTable() != null ? properties.getCustomLightBrightnessTable() : super.getLightBrightnessTable();
    }

    @Override
    public int getMoonPhase(final long worldTime) {
        final long cycleLength = properties.getDayLength() + properties.getNightLength();
        return (int)(worldTime / cycleLength % 8L + 8L) % 8;
    }

    @Override
    public void setWorldTime(final long time) {
        super.setWorldTime(WorldProviderJED.getNewWorldTime(time, getWorldTime(), properties));
    }

    @Override
    public void resetRainAndThunder() {
        if(!properties.getDontAdvanceWeatherWhenSleeping()) super.resetRainAndThunder();
    }

    @Override
    public float calculateCelestialAngle(final long worldTime, final float partialTicks) {
        if(properties.getUseCustomDayCycle()) return WorldProviderJED.calculateCelestialAngle(world, properties, properties.getDayLength() + properties.getNightLength(), worldTime, partialTicks);
        return properties.getUseCustomCelestialAngleRange() ? WorldProviderJED.getCustomCelestialAngleValue(world, properties, properties.getDayLength() + properties.getNightLength(), worldTime, partialTicks) : 0.5f;
    }

    @Override
    public void setAllowedSpawnTypes(final boolean allowHostileIn, final boolean allowPeacefulIn) {
        boolean allowHostile = world.getWorldInfo().getDifficulty() != EnumDifficulty.PEACEFUL;
        boolean allowPeaceful = allowPeacefulIn;

        @Nullable final JEDWorldProperties props = JEDWorldProperties.getPropertiesIfExists(getDimension());
        if(props != null) {
            @Nullable final Boolean hostiles = props.canSpawnHostiles();
            @Nullable final Boolean peaceful = props.canSpawnPeacefulMobs();

            if(hostiles != null) allowHostile = hostiles;
            if(peaceful != null) allowPeaceful = peaceful;
        }

        super.setAllowedSpawnTypes(allowHostile, allowPeaceful);
    }

    @Override
    public boolean canCoordinateBeSpawn(final int x, final int z) {
        return Boolean.TRUE.equals(properties.ignoreSpawnSuitability()) || super.canCoordinateBeSpawn(x, z);
    }

    @Override
    public boolean canDoLightning(@Nonnull final Chunk chunk) {
        return WorldProviderJED.getBooleanOrDefault(properties.canDoLightning(), super.canDoLightning(chunk));
    }

    @Override
    public boolean canDoRainSnowIce(@Nonnull final Chunk chunk) {
        return WorldProviderJED.getBooleanOrDefault(properties.canDoRainSnowIce(), super.canDoRainSnowIce(chunk));
    }

    @Override
    public boolean canBlockFreeze(@Nonnull final BlockPos pos, final boolean byWater) {
        @Nullable final Boolean val = properties.canDoRainSnowIce();
        return val != null ? val && WorldUtils.canBlockFreeze(world, pos, byWater) : super.canBlockFreeze(pos, byWater);
    }

    @Override
    public boolean canSnowAt(@Nonnull final BlockPos pos, final boolean checkLight) {
        @Nullable final Boolean val = properties.canDoRainSnowIce();
        return val != null ? val && WorldUtils.canSnowAt(world, pos) : super.canSnowAt(pos, checkLight);
    }

    @Override
    public boolean doesWaterVaporize() {
        return WorldProviderJED.getBooleanOrDefault(properties.doesWaterVaporize(), super.doesWaterVaporize());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean doesXZShowFog(final int x, final int z) {
        return properties.getHasPerBiomeFog() ? properties.doesBiomeHaveFog(world.getBiome(new BlockPos(x, 0, z))) : WorldProviderJED.getBooleanOrDefault(properties.getHasXZFog(), super.doesXZShowFog(x, z));
    }

    @Override
    public boolean isSurfaceWorld() {
        return WorldProviderJED.getBooleanOrDefault(properties.isSurfaceWorld(), super.isSurfaceWorld());
    }

    @Override
    public int getAverageGroundLevel() {
        return WorldProviderJED.getIntegerOrDefault(properties.getAverageGroundLevel(), super.getAverageGroundLevel());
    }

    @Override
    public double getHorizon() {
        return WorldProviderJED.getDoubleOrDefault(properties.getHorizon(), super.getHorizon());
    }

    @Override
    public double getMovementFactor() {
        return WorldProviderJED.getDoubleOrDefault(properties.getMovementFactor(), super.getMovementFactor());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightness(final float partialTicks) {
        return WorldProviderJED.getFloatOrDefault(properties.getSunBrightness(), super.getSunBrightness(partialTicks));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightnessFactor(final float partialTicks) {
        return WorldProviderJED.getFloatOrDefault(properties.getSunBrightnessFactor(), super.getSunBrightnessFactor(partialTicks));
    }

    @Override
    public boolean shouldClientCheckLighting() {
        return WorldProviderJED.getBooleanOrDefault(properties.shouldClientCheckLight(), super.shouldClientCheckLighting());
    }

    @Override
    public boolean shouldMapSpin(@Nonnull final String entity, final double x, final double z, final double rotation) {
        return !isSurfaceWorld();
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public MusicTicker.MusicType getMusicType() {
        @Nullable final MusicTicker.MusicType music = ClientUtils.getMusicTypeFromProperties(properties);
        return music != null ? music : super.getMusicType();
    }

    @Nonnull
    @Override
    public BlockPos getSpawnCoordinate() {
        return world.getSpawnPoint();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getCloudHeight() {
        return properties.getCloudHeight();
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getSkyColor(@Nonnull final Entity cameraEntity, final float partialTicks) {
        @Nonnull final Vec3d base = super.getSkyColor(cameraEntity, partialTicks);
        @Nullable final Vec3d skyColor = properties.getSkyColor();

        return skyColor != null ? new Vec3d(base.x * skyColor.x, base.y * skyColor.y, base.z * skyColor.z) : base;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getCloudColor(final float partialTicks) {
        @Nonnull final Vec3d base = super.getCloudColor(partialTicks);
        @Nullable final Vec3d cloudColor = properties.getCloudColor();

        return cloudColor != null ? new Vec3d(base.x * cloudColor.x, base.y * cloudColor.y, base.z * cloudColor.z) : base;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getFogColor(final float celestialAngle, final float partialTicks) {
        @Nullable final Vec3d fogColor = properties.getFogColor();
        if(fogColor != null) return getFogColor(world, celestialAngle, partialTicks, fogColor.x, fogColor.y, fogColor.z, NetherAPIFogColorEvent.Nether::new)
                .scale(MathHelper.clamp(Math.cos(celestialAngle * Math.PI * 2) * 2 + 0.5, 0, 1) * 0.94 + 0.06);

        return super.getFogColor(celestialAngle, partialTicks);
    }
}
