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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import com.google.common.collect.Sets;
import git.jbredwards.nether_api.api.block.IConditionalParticleColor;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.objectweb.asm.tree.FieldInsnNode;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Use "Blocks.GRASS" if the block shouldn't have its particles colored
 * @author jbred
 *
 */
public final class TransformerParticleDigging implements ITransformer
{
    @Nonnull
    public static final Set<String> NAMES = Sets.newHashSet("field_174847_a", "sourceState", "blockState");

    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("<init>"), (method, insn) -> {
            /*
             * Constructor: (changes are around line 24)
             * Old code:
             * this.sourceState = state;
             *
             * New code:
             * // Use "Blocks.GRASS" if the block shouldn't have its particles colored.
             * this.sourceState = Hooks.replaceIfGrass(state);
             */
            if(insn.getOpcode() == PUTFIELD && NAMES.contains(((FieldInsnNode)insn).name)) {
                method.instructions.insertBefore(insn, genHookMethod("replaceIfGrass", "(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static IBlockState replaceIfGrass(@Nonnull final IBlockState state) {
            return !(state.getBlock() instanceof IConditionalParticleColor) || ((IConditionalParticleColor)state.getBlock()).particleUseBlockColor(state) ? state : Blocks.GRASS.getDefaultState();
        }
    }
}
