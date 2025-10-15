/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Support modded "End Stone"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockChorusFlower implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, true, classNode -> {
            @Nonnull final IntegerHolder index = new IntegerHolder();
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "updateTick" : "func_180650_b"), (method, insn) -> {
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "END_STONE" : "field_150377_bs")) {
                    /*
                     * Old code:
                     * if (block == Blocks.END_STONE)
                     * {
                     *     flag = true;
                     * }
                     *
                     * New code:
                     * // Support modded "End Stone"-like soil blocks.
                     * if (block == Hooks.getSoilBlock(this, worldIn, pos.down(), iblockstate, Blocks.END_STONE))
                     * {
                     *     flag = true;
                     * }
                     */
                    if(++index.value == 1) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 9));
                        method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                    }
                    /*
                     * Old code:
                     * if (block1 == Blocks.END_STONE)
                     * {
                     *     flag1 = true;
                     * }
                     *
                     * New code:
                     * // Support modded "End Stone"-like soil blocks.
                     * if (block1 == Hooks.getSoilBlock(this, worldIn, pos.down(j + 1), Blocks.END_STONE))
                     * {
                     *     flag1 = true;
                     * }
                     */
                    else {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 11));
                        method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                        method.instructions.insertBefore(insn, new InsnNode(IADD));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177979_c", "(I)Lnet/minecraft/util/math/BlockPos;", false));
                        method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                        return BreakType.METHODS;
                    }
                }

                return BreakType.CONTINUE;
            });
            /*
             * Old code:
             * if (block != Blocks.CHORUS_PLANT && block != Blocks.END_STONE)
             * {
             *     ...
             * }
             *
             * New code:
             * // Support modded "End Stone"-like soil blocks.
             * if (block != Blocks.CHORUS_PLANT && block != Hooks.getSoilBlock(this, worldIn, pos.down(), iblockstate, Blocks.END_STONE))
             * {
             *     ...
             * }
             */
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "canSurvive" : "func_185606_b"), (method, insn) -> {
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "END_STONE" : "field_150377_bs")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                    method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                    method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
            /*
             * New code:
             * public class BlockChorusFlower extends Block implements net.minecraftforge.common.IPlantable
             * {
             *     ...
             *
             *     @ASMGenerated
             *     public IBlockState getPlant(IBlockAccess world, BlockPos pos)
             *     {
             *         return this.getDefaultState();
             *     }
             *
             *     @ASMGenerated
             *     public net.minecraftforge.common.EnumPlantType getPlantType(IBlockAccess world, BlockPos pos)
             *     {
             *         return git.jbredwards.nether_api.api.util.PlantUtils.END_PLANT_TYPE;
             *     }
             * }
             */
            transformPlantable(classNode, "END_PLANT_TYPE");
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Block getSoilBlock(@Nonnull final IPlantable plantable, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final Block fallback) {
            return getSoilBlock(plantable, world, pos, world.getBlockState(pos), fallback);
        }

        @Nonnull
        public static Block getSoilBlock(@Nonnull final IPlantable plantable, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final Block fallback) {
            return state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, plantable) ? state.getBlock() : fallback;
        }
    }
}
