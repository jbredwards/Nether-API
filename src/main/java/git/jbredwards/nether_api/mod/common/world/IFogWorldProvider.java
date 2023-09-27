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

        final int originX = MathHelper.fastFloor(entityPos.x), originZ = MathHelper.fastFloor(entityPos.z);
        final double originDiffX = entityPos.x - originX, originDiffZ = entityPos.z - originZ;
        final int[] weights = {0, 1, 4, 6, 4, 1, 0};

        Vec3d color = Vec3d.ZERO;
        double totalWeight = 0;

        for(int offsetX = 0; offsetX < 6; offsetX++) {
            final double weightX = originDiffX * (weights[offsetX] - weights[offsetX + 1]) + weights[offsetX];
            final int posX = originX + offsetX - 3;

            for(int offsetZ = 0; offsetZ < 6; offsetZ++) {
                final double weightZ = originDiffZ * (weights[offsetZ] - weights[offsetZ + 1]) + weights[offsetZ];
                final int posZ = originZ + offsetZ - 3;

                final Biome biome = world.getBiome(new BlockPos(posX, 0, posZ));
                final NetherAPIFogColorEvent event = eventConstructor.create(biome, world, celestialAngle, partialTicks);

                event.fogR = defaultR;
                event.fogG = defaultG;
                event.fogB = defaultB;

                final double weight = weightX * weightZ;
                totalWeight += weight;
                color = color.add((MinecraftForge.EVENT_BUS.post(event) ? new Vec3d(event.fogR, event.fogG, event.fogB)
                        : getDefaultFogColor(biome, celestialAngle, partialTicks, defaultR, defaultG, defaultB)).scale(weight));
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
