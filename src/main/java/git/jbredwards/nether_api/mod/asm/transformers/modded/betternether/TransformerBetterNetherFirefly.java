/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.commons.GeneratorAdapter;

import javax.annotation.Nonnull;

/**
 * Fix BetterNether firefly spawning code
 * @author jbred
 *
 */
public final class TransformerBetterNetherFirefly implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "getCanSpawnHere" : "func_70601_bi"), (method, insn) -> {
            // remove existing body data
            method.instructions.clear();
            if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
            if(method.localVariables != null) method.localVariables.clear();
            if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
            if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

            // write new body data
            @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
            generator.visitInsn(ICONST_1);
            generator.visitInsn(IRETURN);
            return BreakType.METHODS;
        });
    }
}
