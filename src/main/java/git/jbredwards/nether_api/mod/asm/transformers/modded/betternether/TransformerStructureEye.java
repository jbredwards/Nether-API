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

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't let eye vines carve through terrain
 * @author jbred
 *
 */
public final class TransformerStructureEye implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, false, classNode -> {
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generate")) {
                    final int height = getLocalVar(method, "height", 4);
                    final int vineState = getLocalVar(method, "vineState");
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generate:
                         * Old code:
                         * for(int y = 0; y < height; ++y)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Don't let eye vines carve through terrain.
                         * height = Hooks.fixHeight(height, world, pos);
                         * for(int y = 0; y < height; ++y)
                         * {
                         *     ...
                         * }
                         */
                        if(insn.getOpcode() == ASTORE && ((VarInsnNode)insn).var == vineState) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, height));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                            method.instructions.insertBefore(insn, genHookMethod("fixHeight", "(ILnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I"));
                            method.instructions.insertBefore(insn, new VarInsnNode(ISTORE, height));
                            return;
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static int fixHeight(final int height, @Nonnull final World world, @Nonnull final BlockPos pos) {
            for(int i = height; i > 1; i--) if(world.isAirBlock(pos.down(i))) return i == height && world.isAirBlock(pos.down(i + 1)) ? i : (i - 1);
            return 1;
        }
    }
}
