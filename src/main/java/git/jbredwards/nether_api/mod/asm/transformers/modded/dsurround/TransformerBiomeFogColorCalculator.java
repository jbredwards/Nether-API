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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
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
        return transformMethod(basicClass, method -> method.name.equals("calculate"), (method, insn) -> {
            /*
             * Old code:
             * if (color != null)
             * {
             *     ...
             * }
             *
             * New code:
             * // Support Nether API fog color events.
             * if ((color = Hooks.getColor(biome, world, (float)event.getRenderPartialTicks(), false) != null)
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == ALOAD && ((VarInsnNode)insn).var == 14) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 13));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/client/event/EntityViewRenderEvent$FogColors", "getRenderPartialTicks", "()D", false));
                method.instructions.insertBefore(insn, new InsnNode(D2F));
                method.instructions.insertBefore(insn, new InsnNode(ICONST_0));
                method.instructions.insertBefore(insn, genHookMethod("getColor", "(Lorg/orecruncher/dsurround/registry/biome/BiomeInfo;Lnet/minecraft/world/World;FZ)Lorg/orecruncher/lib/Color;"));
                method.instructions.insertBefore(insn, new VarInsnNode(ASTORE, 14));
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
        @Nullable
        public static Color getColor(@Nonnull final BiomeInfo info, @Nonnull final World world, final float partialTicks, final boolean particle) {
            if(world.provider instanceof IFogWorldProvider) {
                @Nonnull final Vec3d biomeColor = ((IFogWorldProvider)world.provider).getFogColorFor(world, world.getCelestialAngle(partialTicks), partialTicks, info.getBiome());
                @Nonnull final Color color = new Color(biomeColor.x, biomeColor.y, biomeColor.z);

                return particle ? color.luminance(0.5f) : color;
            }

            if(particle || info.getHasDust()) return info.getDustColor();
            else return info.getHasFog() ? info.getFogColor() : null;
        }

        public static float getColorScale(final float scale, @Nonnull final World world) {
            return world.provider.isSurfaceWorld() ? scale : 1;
        }
    }
}
