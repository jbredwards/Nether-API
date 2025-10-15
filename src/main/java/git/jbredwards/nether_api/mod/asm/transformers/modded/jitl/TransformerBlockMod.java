/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.jitl;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.journey.init.blocks.JourneyBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Add support for new EnumPlantTypes to Journey Into The Light soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockMod implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "canSustainPlant", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Z", null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            adapter.loadThis();
            adapter.visitVarInsn(ALOAD, 2);
            adapter.visitVarInsn(ALOAD, 3);
            adapter.visitVarInsn(ALOAD, 4);
            adapter.visitVarInsn(ALOAD, 5);
            adapter.visitMethodInsn(INVOKESTATIC, genHookClass(), "canSustainPlant", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Z", false);
            adapter.returnValue();
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canSustainPlant(@Nonnull final Block block, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            final boolean isNetherGrass = block == JourneyBlocks.earthenNetherrack || block == JourneyBlocks.nethicGrass;
            if(isNetherGrass && Blocks.GRASS.canSustainPlant(Blocks.GRASS.getDefaultState(), world, pos, direction, plantable)) return true;
            else if(isNetherGrass || block == JourneyBlocks.bloodRock || block == JourneyBlocks.heatSoil || block == JourneyBlocks.lavaRock || block == JourneyBlocks.nethicanSludge) {
                return Blocks.NETHERRACK.canSustainPlant(Blocks.NETHERRACK.getDefaultState(), world, pos, direction, plantable);
            }

            return plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Cave;
        }
    }
}
