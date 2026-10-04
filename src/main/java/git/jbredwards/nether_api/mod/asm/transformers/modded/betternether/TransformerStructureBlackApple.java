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
import org.objectweb.asm.tree.IntInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't let BetterNether forks place black apple plants in the air
 * @author jbred
 *
 */
public final class TransformerStructureBlackApple implements ITransformer
{
    int index = 0;

    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        index = 0;
        return transformMethod(basicClass, method -> method.name.equals("generate"), (method, insn) -> {
            if(insn instanceof IntInsnNode && ((IntInsnNode)insn).operand == 6 && ++index > 1) ((IntInsnNode)insn).operand = 1;
            return BreakType.CONTINUE;
        });
    }
}
