/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Support modded "End Stone"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerStygianEndPlant implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            classNode.methods.removeIf(method -> method.name.equals(DEOBFUSCATED ? "canBlockStay" : "func_180671_f"));
            transformPlantable(classNode, "END_PLANT_TYPE");
        });
    }
}
