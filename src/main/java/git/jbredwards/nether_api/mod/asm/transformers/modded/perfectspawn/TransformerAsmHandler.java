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

package git.jbredwards.nether_api.mod.asm.transformers.modded.perfectspawn;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Use the fallback value
 * @author jbred
 *
 */
public final class TransformerAsmHandler implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        /*
         * Old code:
         * return config != null ? config.getInitialSpawnDimension() : 0;
         *
         * New code:
         * // Use the fallback value.
         * return config != null ? config.getInitialSpawnDimension() : original;
         */
        return transformMethod(basicClass, method -> method.name.equals("overrideInitialDimension"), (method, insn) -> {
            if(insn.getOpcode() == ICONST_0) {
                method.instructions.insert(insn, new VarInsnNode(ILOAD, 0));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
