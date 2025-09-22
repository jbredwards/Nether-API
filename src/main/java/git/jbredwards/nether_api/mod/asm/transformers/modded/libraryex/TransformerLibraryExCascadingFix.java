/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.libraryex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;

import javax.annotation.Nonnull;

/**
 * Fix cascading world gen problems with LibraryEx<p>
 * Note that some (like the 32x32 NetherEx villages) naturally can't be fixed due to being larger than 16x16
 * @author jbred
 *
 */
public final class TransformerLibraryExCascadingFix implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        /*
         * Old code:
         * float sizeX = structureSize.getX() + 2;
         * float sizeY = structureSize.getY() + 1;
         * float sizeZ = structureSize.getZ() + 2;
         *
         * New code:
         * //fix bad structure size
         * float sizeX = structureSize.getX();
         * float sizeY = structureSize.getY() + 1;
         * float sizeZ = structureSize.getZ();
         */
        return transformMethod(basicClass, method -> true, (method, insn) -> {
            if(insn.getOpcode() == ICONST_2 && insn.getNext().getOpcode() == IADD) {
                method.instructions.remove(insn.getNext());
                method.instructions.remove(insn);
            }

            return BreakType.CONTINUE;
        });
    }
}
