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

package git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft;

import com.legacy.nethercraft.blocks.BlocksNether;
import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack"-like and "Soul Sand"-like soil blocks, as well as Heat Sand
 * @author jbred
 *
 */
public final class TransformerBlockNetherReed implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"), (method, insn) -> {
            /*
             * Old code:
             * else if (block != BlocksNether.nether_dirt && block != Blocks.NETHERRACK && block != Blocks.SOUL_SAND)
             * {
             *     return false;
             * }
             *
             * New code:
             * // Support modded "Netherrack"-like and "Soul Sand"-like soil blocks, as well as Heat Sand.
             * else if (block != Hooks.getDirt(this, worldIn, pos, state, BlocksNether.nether_dirt) && block != Blocks.NETHERRACK && block != Blocks.SOUL_SAND)
             * {
             *     return false;
             * }
             */
            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("nether_dirt")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insert(insn, genHookMethod("getDirt", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Block getDirt(@Nonnull final Block reed, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState soil, @Nonnull final Block fallback) {
            return soil.getBlock() == BlocksNether.heat_sand || soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, PlantUtils.createPlantable(PlantUtils.NETHER_PLANT_TYPE, reed.getDefaultState())) ? soil.getBlock() : fallback;
        }
    }
}
