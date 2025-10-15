/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Change Biomes O' Plenty's nether biome super classes
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyBiomes implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformedName.startsWith("biomesoplenty") ? transformParent(basicClass, "biomesoplenty/common/biome/nether/BOPHellBiome", "git/jbredwards/nether_api/mod/common/compat/biomesoplenty/AbstractNetherBOPBiome") : basicClass;
    }
}
