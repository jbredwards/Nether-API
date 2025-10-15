/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockGlowWoodSapling implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            classNode.methods.removeIf(method -> method.name.equals(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
            transformPlantable(classNode, "NETHERRACK_PLANT_TYPE");
        });
    }
}
