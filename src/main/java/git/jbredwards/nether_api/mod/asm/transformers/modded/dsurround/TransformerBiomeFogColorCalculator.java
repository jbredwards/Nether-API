/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.world.IFogWorldProvider;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.lib.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Support Nether API fog color events.
 * @author jbred
 *
 */
public final class TransformerBiomeFogColorCalculator implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        @Nonnull final IntegerHolder index = new IntegerHolder();
        return transformMethod(basicClass, method -> method.name.equals("calculate"), (method, insn) -> {
            /*
             * Old code:
             * this.doScan = this.doScan || this.posX != playerX || this.posZ != playerZ;
             *
             * New code:
             * // Copy colors from Vanilla if the world provider controls fog color.
             * this.weightBiomeFog = Hooks.setWeightBiomeFog(this.weightBiomeFog, world, distance);
             * this.biomeFogColor = Hooks.setBiomeFogColor(this.biomeFogColor, world, event, this.weightBiomeFog);
             * this.doScan = Hooks.setDoScan(this.doScan || this.posX != playerX || this.posZ != playerZ);
             */
            if(insn.getOpcode() == PUTFIELD && ((FieldInsnNode)insn).name.equals("doScan") && index.value ++== 0) {
                // this.weightBiomeFog = Hooks.setWeightBiomeFog(this.weightBiomeFog, world, distance);
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "org/orecruncher/dsurround/client/handlers/fog/BiomeFogColorCalculator", "weightBiomeFog", "D"));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 9));
                method.instructions.insertBefore(insn, genHookMethod("setWeightBiomeFog", "(DLnet/minecraft/world/World;I)D"));
                method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "org/orecruncher/dsurround/client/handlers/fog/BiomeFogColorCalculator", "weightBiomeFog", "D"));
                // this.biomeFogColor = Hooks.setBiomeFogColor(this.biomeFogColor, world, event);
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "org/orecruncher/dsurround/client/handlers/fog/BiomeFogColorCalculator", "biomeFogColor", "Lorg/orecruncher/lib/Color;"));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "org/orecruncher/dsurround/client/handlers/fog/BiomeFogColorCalculator", "weightBiomeFog", "D"));
                method.instructions.insertBefore(insn, genHookMethod("setBiomeFogColor", "(Lorg/orecruncher/lib/Color;Lnet/minecraft/world/World;Lnet/minecraftforge/client/event/EntityViewRenderEvent$FogColors;D)Lorg/orecruncher/lib/Color;"));
                method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "org/orecruncher/dsurround/client/handlers/fog/BiomeFogColorCalculator", "biomeFogColor", "Lorg/orecruncher/lib/Color;"));
                // this.doScan = Hooks.setDoScan(this.doScan || this.posX != playerX || this.posZ != playerZ);
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, genHookMethod("setDoScan", "(ZLnet/minecraft/world/World;)Z"));
            }
            /*
             * Old code:
             * float baseScale = MathStuff.clamp(MathStuff.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);
             *
             * New code:
             * // Don't use sky when no sky is present.
             * float baseScale = Hooks.getColorScale(MathStuff.clamp(MathStuff.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F), world);
             */
            else if(insn.getOpcode() == FSTORE && ((VarInsnNode)insn).var == 13) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, genHookMethod("getColorScale", "(FLnet/minecraft/world/World;)F"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        // Used by TransformerStormRenderer.
        @Nullable
        public static Color getColor(@Nonnull final BiomeInfo info, @Nonnull final World world, final float partialTicks, final boolean particle, @Nonnull final BlockPos pos) {
            if(world.provider instanceof IFogWorldProvider) {
                @Nonnull final Vec3d biomeColor = ((IFogWorldProvider)world.provider).getFogColorFor(world, world.getCelestialAngle(partialTicks), partialTicks, info.getBiome(), pos);
                @Nonnull final Color color = new Color(biomeColor.x, biomeColor.y, biomeColor.z);

                return particle ? color.luminance(0.5f) : color;
            }

            if(particle || info.getHasDust()) return info.getDustColor();
            else return info.getHasFog() ? info.getFogColor() : null;
        }

        public static float getColorScale(final float scale, @Nonnull final World world) {
            return world.provider.isSurfaceWorld() ? scale : 1;
        }

        @Nonnull
        public static Color setBiomeFogColor(@Nonnull final Color biomeFogColor, @Nonnull final World world, @Nonnull final EntityViewRenderEvent.FogColors event, final double scale) {
            return world.provider instanceof IFogWorldProvider ? new Color(event.getRed(), event.getGreen(), event.getBlue()).scale((float)scale) : biomeFogColor;
        }

        public static boolean setDoScan(final boolean doScan, @Nonnull final World world) {
            return doScan && !(world.provider instanceof IFogWorldProvider);
        }

        public static double setWeightBiomeFog(final double weightBiomeFog, @Nonnull final World world, final int distance) {
            return world.provider instanceof IFogWorldProvider ? ((distance << 1 | 1) * (distance << 1 | 1)) : weightBiomeFog;
        }
    }
}
