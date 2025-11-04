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

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.FieldInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Prevent BetterNether from resetting its enabled biomes config cache
 * @author jbred
 *
 */
public final class TransformerBetterNetherConfigLoader implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nullable final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        /*
         * dispose:
         * Old code:
         * enabledBiomes = null;
         *
         * New code:
         * //keep this cached, so this mod can refer to it to know which biomes are enabled
         * --------------------;
         */
        return transformMethod(basicClass, method -> method.name.equals("dispose"), (method, insn) -> {
            if(insn.getOpcode() == PUTSTATIC && (((FieldInsnNode)insn).name.equals("enabledBiomes") || ((FieldInsnNode)insn).name.equals("registerBiomes"))) {
                method.instructions.remove(insn.getPrevious());
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
