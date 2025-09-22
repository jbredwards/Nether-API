/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.FieldInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Prevent BetterNether from resetting its enabled biomes config cache
 * @author jbred
 *
 */
public final class TransformerBetterNetherConfigLoader implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nullable final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        /*
         * dispose:
         * Old code:
         * enabledBiomes = null;
         *
         * New code:
         * //keep this cached, so this mod can refer to it to know which biomes are enabled
         * --------------------;
         */
        return transformMethod(basicClass, method -> method.name.equals("dispose"), (method, insn) -> {
            if(insn.getOpcode() == PUTSTATIC && (((FieldInsnNode)insn).name.equals("enabledBiomes") || ((FieldInsnNode)insn).name.equals("registerBiomes"))) {
                method.instructions.remove(insn.getPrevious());
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
