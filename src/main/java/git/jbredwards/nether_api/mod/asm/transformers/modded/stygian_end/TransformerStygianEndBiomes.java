/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Change Stygian End's biome super classes
 * @author jbred
 *
 */
public final class TransformerStygianEndBiomes implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformedName.startsWith("fluke.stygian.world.biomes") ? transformParent(basicClass, "net/minecraft/world/biome/Biome", "git/jbredwards/nether_api/mod/common/compat/stygian_end/AbstractStygianEndBiome") : basicClass;
    }
}
