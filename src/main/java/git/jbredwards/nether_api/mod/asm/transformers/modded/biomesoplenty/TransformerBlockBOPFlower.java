/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockBOPFlower implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("canBlockStay"), (method, insn) -> {
            /*
             * Old code:
             * boolean onNetherrack = groundBlock == Blocks.NETHERRACK;
             *
             * New code:
             * // Support modded "Netherrack"-like soil blocks.
             * boolean onNetherrack = groundBlock == Hooks.getSoilBlock(state, groundState, world, pos.down(), Blocks.NETHERRACK));
             */
            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "NETHERRACK" : "field_150424_aL")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Block getSoilBlock(@Nonnull final IBlockState flower, @Nonnull final IBlockState soil, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final Block fallback) {
            return soil.getBlock().canSustainPlant(soil, world, pos, EnumFacing.UP, PlantUtils.createPlantable(PlantUtils.NETHERRACK_PLANT_TYPE, flower)) ? soil.getBlock() : fallback;
        }
    }
}
