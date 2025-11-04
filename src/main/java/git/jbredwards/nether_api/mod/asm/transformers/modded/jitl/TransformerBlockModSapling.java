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

package git.jbredwards.nether_api.mod.asm.transformers.modded.jitl;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.journey.api.block.GroundPredicate;
import net.journey.init.blocks.JourneyBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.slayer.api.block.BlockModGrass;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Allow nether saplings to be planted on the blocks they generate on
 * @author jbred
 *
 */
public final class TransformerBlockModSapling implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            @Nonnull final MethodNode canBlockStayMethod = new MethodNode(ACC_PUBLIC, "canBlockStay", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", null, null);
            @Nonnull final GeneratorAdapter canBlockStay = new GeneratorAdapter(canBlockStayMethod, canBlockStayMethod.access, canBlockStayMethod.name, canBlockStayMethod.desc);
            canBlockStay.loadThis();
            canBlockStay.visitVarInsn(ALOAD, 1);
            canBlockStay.visitVarInsn(ALOAD, 2);
            canBlockStay.visitMethodInsn(INVOKESTATIC, genHookClass(), "canPlaceBlockAt", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false);
            canBlockStay.returnValue();
            classNode.methods.add(canBlockStayMethod);

            @Nonnull final MethodNode canPlaceBlockAtMethod = new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", null, null);
            @Nonnull final GeneratorAdapter canPlaceBlockAt = new GeneratorAdapter(canPlaceBlockAtMethod, canPlaceBlockAtMethod.access, canPlaceBlockAtMethod.name, canPlaceBlockAtMethod.desc);
            canPlaceBlockAt.loadThis();
            canPlaceBlockAt.visitVarInsn(ALOAD, 1);
            canPlaceBlockAt.visitVarInsn(ALOAD, 2);
            canPlaceBlockAt.visitMethodInsn(INVOKESTATIC, genHookClass(), "canPlaceBlockAt", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false);
            canPlaceBlockAt.returnValue();
            classNode.methods.add(canPlaceBlockAtMethod);
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canPlaceBlockAt(@Nonnull final Block block, @Nonnull final World world, @Nonnull final BlockPos pos) {
            @Nonnull final IBlockState below = world.getBlockState(pos.down());
            if(below.getBlock() instanceof BlockModGrass) return true;

            return (below.getMaterial() == Material.GRASS || below.getMaterial() == Material.GROUND) && GroundPredicate.SOLID_SIDE.testGround(world, pos.down(), below, EnumFacing.UP)
            || (block == JourneyBlocks.netherSapling || block == JourneyBlocks.EARTHEN_SAPLING) && GroundPredicate.NETHER.testGround(world, pos.down(), below, EnumFacing.UP);
        }
    }
}
