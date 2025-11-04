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

package git.jbredwards.nether_api.mod.asm.transformers.modded.dynamictrees;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_DynamicTrees implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            transformMethod(classNode, method -> method.name.equals("inNetherRange"), (method, insn) -> {
                if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                    ((IntInsnNode)insn).operand = 255;
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });

            transformMethod(classNode, method -> method.name.equals("findSubterraneanLayerHeights"), (method, insn) -> {
                if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("inNetherRange")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                    method.instructions.insertBefore(insn, genHookMethod("inNetherRange", "(Ljava/lang/Object;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;)Z"));
                    method.instructions.remove(insn);
                }

                return BreakType.CONTINUE;
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean inNetherRange(@Nonnull final Object groundFinder, @Nonnull final BlockPos pos, @Nonnull final World world) {
            return pos.getY() >= 0 && pos.getY() <= world.getActualHeight();
        }
    }
}
