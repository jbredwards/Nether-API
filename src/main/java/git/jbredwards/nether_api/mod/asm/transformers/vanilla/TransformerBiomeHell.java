/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
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
