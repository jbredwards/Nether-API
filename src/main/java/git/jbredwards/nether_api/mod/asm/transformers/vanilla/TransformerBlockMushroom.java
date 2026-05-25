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

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.compat.betternether.BiomeBetterNether;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Allow BetterNether's 3d mushroom feature to have more render options
 * @author jbred
 *
 */
public final class TransformerBlockMushroom implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return FMLLaunchHandler.side().isServer() ? basicClass : transform(basicClass, classNode -> {
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "createBlockState" : "func_180661_e", "()Lnet/minecraft/block/state/BlockStateContainer;", null, null), adapter -> {
                adapter.visitVarInsn(ALOAD, 0);
                adapter.visitMethodInsn(INVOKESTATIC, genHookClass(), "createBlockState", "(Lnet/minecraft/block/Block;)Lnet/minecraft/block/state/BlockStateContainer;", false);
            });
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "getExtendedState", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", null, null), adapter -> {
                adapter.visitVarInsn(ALOAD, 1);
                adapter.visitVarInsn(ALOAD, 2);
                adapter.visitVarInsn(ALOAD, 3);
                adapter.visitMethodInsn(INVOKESTATIC, genHookClass(), "getExtendedState", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", false);
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        @SideOnly(Side.CLIENT)
        public static final IUnlistedProperty<Boolean> IS_BIOME_3D = Properties.toUnlisted(PropertyBool.create("is_biome_3d"));

        @Nonnull
        @SideOnly(Side.CLIENT)
        public static BlockStateContainer createBlockState(@Nonnull final Block block) {
            return new BlockStateContainer.Builder(block).add(IS_BIOME_3D).build();
        }

        @Nonnull
        @SideOnly(Side.CLIENT)
        public static IBlockState getExtendedState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess access, @Nonnull final BlockPos pos) {
            return NetherAPI.isBetterNetherLoaded && state instanceof IExtendedBlockState && ((IExtendedBlockState)state).getUnlistedNames().contains(IS_BIOME_3D)
                    ? ((IExtendedBlockState)state).withProperty(IS_BIOME_3D, access.getBiome(pos) instanceof BiomeBetterNether) : state;
        }
    }
}
