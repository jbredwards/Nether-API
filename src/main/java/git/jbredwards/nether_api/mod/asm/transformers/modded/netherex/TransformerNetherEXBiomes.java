/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Change NetherEx's nether biome super classes
 * @author jbred
 *
 */
public final class TransformerNetherEXBiomes implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformedName.startsWith("logictechcorp") ? transformParent(basicClass, "logictechcorp/netherex/world/biome/BiomeNetherEx", "git/jbredwards/nether_api/mod/common/compat/netherex/AbstractNetherExBiome") : basicClass;
    }
}
