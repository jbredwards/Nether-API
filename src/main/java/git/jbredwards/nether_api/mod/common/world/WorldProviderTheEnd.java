/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.client.audio.TheEndMusicHandler;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
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
public class WorldProviderTheEnd extends WorldProviderEnd implements IAmbienceWorldProvider
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
                    color = color.add((biome instanceof IEndBiome ? ((IEndBiome)biome).getFogColor(celestialAngle, partialTicks) : Vec3d.ZERO).scale(weight));
                }
            }
        }

        return color.scale(1 / totalWeight);
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
