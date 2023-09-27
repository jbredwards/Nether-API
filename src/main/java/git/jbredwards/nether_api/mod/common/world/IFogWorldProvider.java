/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.api.event.NetherAPIFogColorEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public interface IFogWorldProvider
{
    @SideOnly(Side.CLIENT)
    Vec3d getDefaultFogColor(@Nonnull Biome biome, float celestialAngle, float partialTicks, double defaultR, double defaultG, double defaultB);

    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(@Nonnull World world, float celestialAngle, float partialTicks, double defaultR, double defaultG, double defaultB, @Nonnull FogEventSupplier eventConstructor) {
        final Vec3d entityPos = ActiveRenderInfo.projectViewFromEntity(Minecraft.getMinecraft().player, partialTicks);

        final int originX = MathHelper.fastFloor(entityPos.x), originY = MathHelper.fastFloor(entityPos.y), originZ = MathHelper.fastFloor(entityPos.z);
        final Vec3d originDiff = entityPos.subtract(originX, originY, originZ);

        final int[] weights = {0, 1, 4, 6, 4, 1, 0};
        final int weightsSize = weights.length - 1;

        double totalWeight = 0;
        Vec3d color = Vec3d.ZERO;
        Chunk[] chunks = new Chunk[4];

        for(int offsetX = 0; offsetX < weightsSize; offsetX++) {
            final double weightX = originDiff.x * (weights[offsetX] - weights[offsetX + 1]) + weights[offsetX];
            final int posX = originX + offsetX - (weightsSize >> 2);

            for(int offsetZ = 0; offsetZ < weightsSize; offsetZ++) {
                final double weightZ = originDiff.z * (weights[offsetZ] - weights[offsetZ + 1]) + weights[offsetZ];
                final int posZ = originZ + offsetZ - (weightsSize >> 2);

                final int index = (posZ & 1) << 1 | (posX & 1);
                final Biome biome = (chunks[index] != null ? chunks[index] : (chunks[index] = world.getChunk(posX >> 4, posZ >> 4))).getBiome(new BlockPos(posX, 0, posZ), world.getBiomeProvider());
                final NetherAPIFogColorEvent event = eventConstructor.create(biome, world, celestialAngle, partialTicks);

                event.fogR = defaultR;
                event.fogG = defaultG;
                event.fogB = defaultB;
                final Vec3d biomeFogColor = MinecraftForge.EVENT_BUS.post(event) ? new Vec3d(event.fogR, event.fogG, event.fogB) : getDefaultFogColor(biome, celestialAngle, partialTicks, defaultR, defaultG, defaultB);
                for(int offsetY = 0; offsetY < weightsSize; offsetY++) {
                    final double weightY = originDiff.y * (weights[offsetY] - weights[offsetY + 1]) + weights[offsetY];
                    final double weight = weightX * weightY * weightZ;

                    totalWeight += weight;
                    color = color.add(biomeFogColor.scale(weight));
                }
            }
        }

        return color.scale(1 / totalWeight);
    }

    @FunctionalInterface
    interface FogEventSupplier
    {
        @Nonnull
        NetherAPIFogColorEvent create(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn);
    }
}
