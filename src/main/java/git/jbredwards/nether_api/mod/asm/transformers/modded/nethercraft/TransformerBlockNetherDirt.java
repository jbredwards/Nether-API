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

import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Add support for new EnumPlantTypes to Nethercraft's soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockNetherDirt implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "canSustainPlant", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Z", null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            adapter.visitInsn(classNode.name.endsWith("BlockNetherFarmland") ? ICONST_1 : ICONST_0);
            adapter.visitVarInsn(ALOAD, 2);
            adapter.visitVarInsn(ALOAD, 3);
            adapter.visitVarInsn(ALOAD, 4);
            adapter.visitVarInsn(ALOAD, 5);
            adapter.visitMethodInsn(INVOKESTATIC, genHookClass(), "canSustainPlant", "(ZLnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Z", false);
            adapter.returnValue();
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canSustainPlant(final boolean farmland, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            return PlantUtils.sustainsNether(PlantUtils.NETHER_PLANT_TYPE, world, pos, direction, plantable) || farmland && Blocks.FARMLAND.canSustainPlant(Blocks.FARMLAND.getDefaultState(), world, pos, direction, plantable);
        }
    }
}
