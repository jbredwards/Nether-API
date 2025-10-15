/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Allow shrubs to be used in Nether world generation
 * @author jbred
 *
 */
public final class TransformerBlockTallGrass implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "canBlockStay" : "func_180671_f"), (method, insn) -> {
            /*
             * Old code:
             * return super.canBlockStay(worldIn, pos, state);
             *
             * New code:
             * // Allow shrubs to be used in Nether world generation.
             * return Hooks.orSolidIfShrub(super.canBlockStay(worldIn, pos, state), this, worldIn, pos, state);
             */
            if(insn.getOpcode() == IRETURN) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, genHookMethod("orSolidIfShrub", "(ZLnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean orSolidIfShrub(final boolean fallback, @Nonnull final Block block, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
            return fallback || state.getBlock() == block && state.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.DEAD_BUSH && world.isSideSolid(pos.down(), EnumFacing.UP, false);
        }
    }
}
