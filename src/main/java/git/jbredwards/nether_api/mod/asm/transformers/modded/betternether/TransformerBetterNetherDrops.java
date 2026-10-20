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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import org.objectweb.asm.tree.MethodNode;
import paulevs.betternether.blocks.BlockBoneMushroom;
import paulevs.betternether.blocks.BlockMagmaFlower;
import paulevs.betternether.blocks.BlockOrangeMushroom;
import paulevs.betternether.blocks.BlocksRegister;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Move hardcoded block drops to Forge system
 * @author jbred
 *
 */
public final class TransformerBetterNetherDrops implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            transformMethod(classNode, method -> method.name.equals("spawnSeeds"), (method, insn) -> {
                method.instructions.clear();
                method.visitInsn(RETURN);
                return BreakType.METHODS;
            });

            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "getDrops", "(Lnet/minecraft/util/NonNullList;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V", null, null), generator -> {
                generator.loadThis();
                generator.loadArg(0);
                generator.loadArg(1);
                generator.loadArg(2);
                generator.loadArg(3);
                generator.loadArg(4);
                generator.visitMethodInsn(INVOKESPECIAL, classNode.superName, "getDrops", "(Lnet/minecraft/util/NonNullList;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V", false);

                generator.loadArg(0);
                generator.loadArg(3);
                generator.loadArg(1);
                generator.visitFieldInsn(GETSTATIC, "net/minecraft/block/Block", "RANDOM", "Ljava/util/Random;");
                genHookMethod("getRandom", "(Lnet/minecraft/world/IBlockAccess;Ljava/util/Random;)Ljava/util/Random;").accept(generator);
                genHookMethod("addSeeds" + classNode.name.substring(classNode.name.lastIndexOf('/') + 1), "(Lnet/minecraft/util/NonNullList;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V").accept(generator);
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void addSeedsBlockBoneMushroom(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            if(state.getValue(BlockBoneMushroom.AGE) > 1) {
                splitDroppedStack(drops, new ItemStack(state.getBlock(), 2 + random.nextInt(3)));
                splitDroppedStack(drops, new ItemStack(Items.DYE, 1 + random.nextInt(2), 15));
            }
        }

        public static void addSeedsBlockEyeBase(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            splitDroppedStack(drops, new ItemStack(BlocksRegister.BLOCK_EYE_SEED, 1 + random.nextInt(3)));
        }

        public static void addSeedsBlockLucisMushroom(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            if(random.nextInt(4) == 0) drops.add(new ItemStack(BlocksRegister.BLOCK_LUCIS_SPORE));
        }

        public static void addSeedsBlockMagmaFlower(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            if(state.getValue(BlockMagmaFlower.SIZE) == 2) {
                splitDroppedStack(drops, new ItemStack(Items.MAGMA_CREAM, 1 + random.nextInt(3)));
                splitDroppedStack(drops, new ItemStack(state.getBlock(), 1 + random.nextInt(3)));
            }
        }

        public static void addSeedsBlockOrangeMushroom(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            if(state.getValue(BlockOrangeMushroom.SIZE) > 2) {
                splitDroppedStack(drops, new ItemStack(state.getBlock(), 1 + random.nextInt(3)));
                splitDroppedStack(drops, new ItemStack(Items.DYE, 1 + random.nextInt(2), 5));
                splitDroppedStack(drops, new ItemStack(Items.DYE, 1 + random.nextInt(2), 14));
            }
        }

        public static void addSeedsBlockStalagnateBase(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            if(random.nextInt(4) == 0) drops.add(new ItemStack(BlocksRegister.BLOCK_STALAGNATE_SEED));
        }

        public static void addSeedsBlockStalagnateBottom(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockState state, @Nonnull final Random random) {
            // NO-OP
        }

        @Nonnull
        public static Random getRandom(@Nonnull final IBlockAccess access, @Nonnull final Random fallback) {
            return access instanceof World ? ((World)access).rand : fallback;
        }

        // Helper.
        public static void splitDroppedStack(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final ItemStack stack) {
            for(int i = stack.getCount(); i > 0; i--) drops.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
        }
    }
}
