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

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import paulevs.betternether.blocks.BlockLucisMushroom;

import javax.annotation.Nonnull;

/**
 * Fix BetterNether RotN lucis mushroom rotation
 * @author jbred
 *
 */
public final class TransformerBetterNetherRotNLucisMushroom implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            @Nonnull final IntegerHolder index = new IntegerHolder();
            classNode.methods.removeIf(method -> method.name.equals(DEOBFUSCATED ? "neighborChanged" : "func_189540_a"));
            transformMethod(classNode, method -> method.name.equals("generate"), (method, insn) -> {
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "HORIZONTALS" : "field_176754_o")) {
                    if(++index.value == 3) {
                        ((FieldInsnNode)insn).owner = genHookClass();
                        ((FieldInsnNode)insn).name = "OFFSET_FACINGS";
                    }
                    else if(index.value == 4) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 6));
                        method.instructions.insertBefore(insn, genHookMethod("gen2x2", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)[Lnet/minecraft/util/EnumFacing;"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }
                }

                return BreakType.CONTINUE;
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static final EnumFacing[] OFFSET_FACINGS = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.EAST };

        @Nonnull
        public static EnumFacing[] gen2x2(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState corner) {
            if(world.isAirBlock(pos.west()))
                world.setBlockState(pos.west(), corner.withProperty(BlockLucisMushroom.FACING, BlockLucisMushroom.EnumDir.EAST), Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
            if(world.isAirBlock(pos.south()))
                world.setBlockState(pos.south(), corner.withProperty(BlockLucisMushroom.FACING, BlockLucisMushroom.EnumDir.WEST), Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
            if(world.isAirBlock(pos.south().west()))
                world.setBlockState(pos.south().west(), corner.withProperty(BlockLucisMushroom.FACING, BlockLucisMushroom.EnumDir.NORTH), Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
            return new EnumFacing[0];
        }
    }
}
