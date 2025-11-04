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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Fix MC-31681 (Fog and clouds darken when indoors or under trees)
 * @author jbred
 *
 */
public final class TransformerEntityRenderer implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "updateRenderer" : "func_78464_a"), (method, insn) -> {
            /*
             * updateRenderer: (changes are around line 355)
             * Old code:
             * {
             *     ...
             * }
             *
             * New code:
             * // Fix MC-31681 (Fog and clouds darken when indoors or under trees)
             * {
             *     ...
             *     this.fogColor1 = 1F;
             * }
             */
            if(insn.getOpcode() == RETURN) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new InsnNode(FCONST_1));
                method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/client/renderer/EntityRenderer", DEOBFUSCATED ? "fogColor1" : "field_78539_ae", "F"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
