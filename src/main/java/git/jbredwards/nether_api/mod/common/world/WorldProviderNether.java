/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.client.audio.NetherMusicHandler;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNether;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorNether;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
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
public class WorldProviderNether extends WorldProviderHell implements IAmbienceWorldProvider
{
    @Override
    public void init() {
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
        final Vec3d entityPos = ActiveRenderInfo.projectViewFromEntity(Minecraft.getMinecraft().player, partialTicks);

        final BlockPos originFloored = new BlockPos(entityPos);
        final Vec3d originDiff = entityPos.subtract(originFloored.getX(), originFloored.getY(), originFloored.getZ());

        final int[] weights = {0, 1, 4, 6, 4, 1, 0};
        final int weightsSize = weights.length - 1;

        double totalWeight = 0;
        Vec3d color = Vec3d.ZERO;

        for(int offsetX = 0; offsetX < weightsSize; offsetX++) {
            final double weightX = originDiff.x * (weights[offsetX] - weights[offsetX + 1]) + weights[offsetX];
            final int posX = originFloored.getX() + offsetX - (weightsSize >> 2);

            for(int offsetY = 0; offsetY < weightsSize; offsetY++) {
                final double weightY = originDiff.y * (weights[offsetY] - weights[offsetY + 1]) + weights[offsetY];
                final int posY = originFloored.getY() + offsetY - (weightsSize >> 2);

                for(int offsetZ = 0; offsetZ < weightsSize; offsetZ++) {
                    final double weightZ = originDiff.z * (weights[offsetZ] - weights[offsetZ + 1]) + weights[offsetZ];
                    final int posZ = originFloored.getZ() + offsetZ - (weightsSize >> 2);

                    final double weight = weightX * weightY * weightZ;
                    totalWeight += weight;

                    final Biome biome = world.getBiome(new BlockPos(posX, posY, posZ));
                    color = color.add((biome instanceof INetherBiome ? ((INetherBiome)biome).getFogColor(celestialAngle, partialTicks) : new Vec3d(0.2, 0.03, 0.03)).scale(weight));
                }
            }
        }

        return color.scale(1 / totalWeight);
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
        return !NetherAPI.isNetherExLoaded || NetherExHandler.doesXZShowFog(); //preserve NetherEx's fog settings if that mod is present
    }
}
