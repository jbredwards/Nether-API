/*
 * Copyright (C) <2025 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeHell;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * All BiomeHell instances now use netherrack as their top and filler blocks by default
 * @author jbred
 *
 */
public final class TransformerBiomeHell implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("<init>"), (method, insn) -> {
            /*
             * Constructor: (changes are around line 21)
             * Old code:
             * {
             *     ...
             * }
             *
             * New code:
             * //set default top and filler blocks
             * {
             *     ...
             *     Hooks.setDefaultTopAndFillerBlocks(this);
             * }
             */
            if(insn.getOpcode() == RETURN) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, genHookMethod("setDefaultTopAndFillerBlocks", "(Lnet/minecraft/world/biome/BiomeHell;)V"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void setDefaultTopAndFillerBlocks(@Nonnull BiomeHell biome) {
            biome.topBlock = Blocks.NETHERRACK.getDefaultState();
            biome.fillerBlock = Blocks.NETHERRACK.getDefaultState();
        }
    }
}
