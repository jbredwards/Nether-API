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
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
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
        return getFogColor(world, celestialAngle, partialTicks);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getBiomeFogColor(float celestialAngle, float partialTicks, @Nonnull Biome biome) {
        return biome instanceof INetherBiome ? ((INetherBiome)biome).getFogColor(celestialAngle, partialTicks) : getDefaultFogColor(celestialAngle, partialTicks);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getDefaultFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.2, 0.03, 0.03);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public NetherAPIFogColorEvent createEvent(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn) {
        return new NetherAPIFogColorEvent.Nether(biomeIn, worldIn, celestialAngleIn, partialTicksIn);
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
}
