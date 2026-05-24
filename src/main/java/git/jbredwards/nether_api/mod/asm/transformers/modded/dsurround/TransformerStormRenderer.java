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

package git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Support Nether API fog color events.
 * @author jbred
 *
 */
public final class TransformerStormRenderer implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("render"), (method, insn) -> {
            /*
             * Old code:
             * color = biome.getDustColor();
             *
             * New code:
             * // Support Nether API fog color events.
             * color = Hooks.getColor(biome, worldClient, partialTicks, true);
             */
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("getDustColor")) {
                method.instructions.insert(insn, genHookMethod("getColor", "(Lorg/orecruncher/dsurround/registry/biome/BiomeInfo;Lnet/minecraft/world/World;FZ)Lorg/orecruncher/lib/Color;"));
                method.instructions.insert(insn, new InsnNode(ICONST_1));
                method.instructions.insert(insn, new VarInsnNode(FLOAD, 2));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 5));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @Nonnull
    @Override
    public String genHookClass() { return new TransformerBiomeFogColorCalculator().genHookClass(); }
}
