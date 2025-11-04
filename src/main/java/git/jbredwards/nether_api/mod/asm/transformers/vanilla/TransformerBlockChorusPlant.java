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
import io.netty.util.internal.IntegerHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChorusPlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Support modded "End Stone"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockChorusPlant implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, true, classNode -> {
            @Nonnull final IntegerHolder index = new IntegerHolder();
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "canSurviveAt" : "func_185608_b"), (method, insn) -> {
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "END_STONE" : "field_150377_bs")) {
                    /*
                     * Old code:
                     * if (block1 == this || block1 == Blocks.END_STONE)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Support modded "End Stone"-like soil blocks.
                     * if (block1 == this || block1 == Hooks.getSoilBlock(this, worldIn, blockpos.down(), Blocks.END_STONE))
                     * {
                     *     ...
                     * }
                     */
                    if(++index.value == 1) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 7));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                        method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                    }
                    /*
                     * Old code:
                     * return block2 == this || block2 == Blocks.END_STONE;
                     *
                     * New code:
                     * // Support modded "End Stone"-like soil blocks.
                     * return block2 == this || block2 == Hooks.getSoilBlock(this, worldIn, pos.down(), Blocks.END_STONE);
                     */
                    else {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                        method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                        return BreakType.METHODS;
                    }
                }

                return BreakType.CONTINUE;
            });
            /*
             * Old code:
             * return state.withProperty(DOWN, Boolean.valueOf(block == this || block == Blocks.CHORUS_FLOWER || block == Blocks.END_STONE))...
             *
             * New code:
             * // Support modded "End Stone"-like soil blocks.
             * return state.withProperty(DOWN, Boolean.valueOf(block == this || block == Blocks.CHORUS_FLOWER || block == Hooks.getSoilBlock(this, worldIn, pos.down(), Blocks.END_STONE)))...
             */
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "getActualState" : "func_176221_a"), (method, insn) -> {
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "END_STONE" : "field_150377_bs")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                    method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                    method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
            /*
             * New code:
             * // Use state properties instead of hardcoded logic.
             * @ASMOverwrite
             * @SideOnly(Side.CLIENT)
             * public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
             * {
             *     return Hooks.shouldSideBeRendered(blockState, side);
             * }
             */
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "shouldSideBeRendered" : "func_176225_a"), (method, insn) -> {
                // remove existing body data
                method.instructions.clear();
                if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
                if(method.localVariables != null) method.localVariables.clear();
                if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
                if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

                // write new body data
                @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 4);
                generator.visitMethodInsn(INVOKESTATIC, genHookClass(), "shouldSideBeRendered", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;)Z", false);
                generator.returnValue();
                return BreakType.METHODS;
            });
            /*
             * New code:
             * public class BlockChorusPlant extends Block implements net.minecraftforge.common.IPlantable
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
            return TransformerBlockChorusFlower.Hooks.getSoilBlock(plantable, world, pos, fallback);
        }

        @SideOnly(Side.CLIENT)
        public static boolean shouldSideBeRendered(@Nonnull final IBlockState state, @Nonnull final EnumFacing side) {
            switch(side) {
                case DOWN: return !state.getValue(BlockChorusPlant.DOWN);
                case UP: return !state.getValue(BlockChorusPlant.UP);
                case NORTH: return !state.getValue(BlockChorusPlant.NORTH);
                case SOUTH: return !state.getValue(BlockChorusPlant.SOUTH);
                case WEST: return !state.getValue(BlockChorusPlant.WEST);
                case EAST: return !state.getValue(BlockChorusPlant.EAST);
            }

            return true;
        }
    }
}
