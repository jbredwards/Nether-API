/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.jitl;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Allow Journey Into The Light to use real biomes instead of pseudo-biomes
 * @author jbred
 *
 */
public final class TransformerJITLGenerator implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transform(basicClass, classNode -> classNode.methods.removeIf(method -> method.name.equals("onPopulate") || method.name.equals("onPrePopulate")));
    }
}
