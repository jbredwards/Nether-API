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
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(@Nonnull World world, float celestialAngle, float partialTicks) {
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

                final double weight = weightX * weightZ;
                totalWeight += weight;
                color = color.add(getFogColorFor(world, celestialAngle, partialTicks, world.getBiome(new BlockPos(posX, 0, posZ))).scale(weight));
            }
        }

        return color.scale(1 / totalWeight);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColorFor(@Nonnull World world, float celestialAngle, float partialTicks, @Nonnull Biome biome) {
        final NetherAPIFogColorEvent event = createEvent(biome, world, celestialAngle, partialTicks);
        final Vec3d defaultFogColor = getDefaultFogColor(celestialAngle, partialTicks);
        event.fogR = defaultFogColor.x;
        event.fogG = defaultFogColor.y;
        event.fogB = defaultFogColor.z;
        return MinecraftForge.EVENT_BUS.post(event) ? new Vec3d(event.fogR, event.fogG, event.fogB) : getBiomeFogColor(celestialAngle, partialTicks, biome);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    Vec3d getBiomeFogColor(float celestialAngle, float partialTicks, @Nonnull Biome biome);

    @Nonnull
    @SideOnly(Side.CLIENT)
    Vec3d getDefaultFogColor(float celestialAngle, float partialTicks);

    @Nonnull
    @SideOnly(Side.CLIENT)
    NetherAPIFogColorEvent createEvent(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn);
}
