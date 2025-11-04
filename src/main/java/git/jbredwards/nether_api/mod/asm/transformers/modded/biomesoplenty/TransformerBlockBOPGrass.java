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

import biomesoplenty.common.block.BlockBOPGrass;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import org.objectweb.asm.commons.GeneratorAdapter;

import javax.annotation.Nonnull;

/**
 * Add support for new EnumPlantTypes to BOP soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockBOPGrass implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("canSustainPlant"), (method, insn) -> {
            // remove existing body data
            method.instructions.clear();
            if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
            if(method.localVariables != null) method.localVariables.clear();
            if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
            if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

            // write new body data
            @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
            generator.visitVarInsn(ALOAD, 1);
            generator.visitVarInsn(ALOAD, 2);
            generator.visitVarInsn(ALOAD, 3);
            generator.visitVarInsn(ALOAD, 4);
            generator.visitVarInsn(ALOAD, 5);
            generator.visitMethodInsn(INVOKESTATIC, genHookClass(), "canSustainPlant", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Z", false);
            generator.returnValue();
            return BreakType.METHODS;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canSustainPlant(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            if(Blocks.GRASS.canSustainPlant(Blocks.GRASS.getDefaultState(), world, pos, direction, plantable) || plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Desert) return true; // From BOP.

            switch((BlockBOPGrass.BOPGrassType)state.getValue(BlockBOPGrass.VARIANT)) {
                case MYCELIAL_NETHERRACK: if(Blocks.MYCELIUM.canSustainPlant(Blocks.MYCELIUM.getDefaultState(), world, pos, direction, plantable)) return true;
                case OVERGROWN_NETHERRACK: return Blocks.NETHERRACK.canSustainPlant(Blocks.NETHERRACK.getDefaultState(), world, pos, direction, plantable);
                case SPECTRAL_MOSS: return Blocks.END_STONE.canSustainPlant(Blocks.END_STONE.getDefaultState(), world, pos, direction, plantable);
                default: return false;
            }
        }
    }
}
