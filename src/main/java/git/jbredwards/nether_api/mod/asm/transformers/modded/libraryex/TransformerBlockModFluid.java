/*
 * Copyright (C) <2026 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.libraryex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Assign map color from the block properties
 * @author jbred
 *
 */
public final class TransformerBlockModFluid implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("<init>"), (method, insn) -> {
            /*
             * Old code:
             * super(fluid, properties.getMaterial());
             *
             * New code:
             * // Assign map color from the block properties.
             * super(fluid, properties.getMaterial(), properties.getMapColor());
             */
            if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("<init>") && !((MethodInsnNode)insn).desc.endsWith("r;)V")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "logictechcorp/libraryex/block/property/BlockProperties", "getMapColor", "()Lnet/minecraft/block/material/MapColor;", false));
                ((MethodInsnNode)insn).desc = "(Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/block/material/Material;Lnet/minecraft/block/material/MapColor;)V";
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
