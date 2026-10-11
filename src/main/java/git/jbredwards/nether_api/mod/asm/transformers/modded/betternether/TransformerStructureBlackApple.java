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
import io.netty.util.internal.IntegerHolder;
import net.minecraft.block.material.Material;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't let BetterNether forks place black apple plants in the air
 * @author jbred
 *
 */
public final class TransformerStructureBlackApple implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        @Nonnull final IntegerHolder index = new IntegerHolder();
        return transformMethod(basicClass, method -> method.name.equals("generate"), (method, insn) -> {
            if(insn instanceof IntInsnNode && ((IntInsnNode)insn).operand == 6 && ++index.value > 1) ((IntInsnNode)insn).operand = 1;
            // Prevent black apples from sometimes generating in lava.
            else if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "isReplaceable" : "func_76222_j")) {
                method.instructions.insert(insn, genHookMethod("isReplaceable", "(Lnet/minecraft/block/material/Material;)Z"));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean isReplaceable(@Nonnull final Material material) {
            return material.isReplaceable() && !material.isLiquid();
        }
    }
}
