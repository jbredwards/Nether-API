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

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.common.block.BlockBOPSapling;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockBOPSapling implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("canBlockStay"), (method, insn) -> {
            /*
             * Old code:
             * return BlockQueries.fertile.matches(world, pos.down());
             *
             * New code:
             * // Support modded "Netherrack"-like soil blocks.
             * return Hooks.getCondition(state).matches(world, pos.down());
             */
            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("fertile")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, genHookMethod("getCondition", "(Lnet/minecraft/block/state/IBlockState;)Lbiomesoplenty/api/block/IBlockPosQuery;"));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static IBlockPosQuery getCondition(@Nonnull final IBlockState sapling) {
            return sapling.getValue((IProperty<?>)((BlockBOPSapling)sapling.getBlock()).variantProperty) == BOPTrees.HELLBARK ? BlockQueries.fertileOrNetherrack : BlockQueries.fertile;
        }
    }
}
