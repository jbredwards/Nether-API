/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
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
