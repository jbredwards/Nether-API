/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
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
