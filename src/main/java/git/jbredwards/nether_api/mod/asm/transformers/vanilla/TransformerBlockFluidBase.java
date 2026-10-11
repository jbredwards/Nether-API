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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't render white fog for fluids without a color
 * @author jbred
 *
 */
public final class TransformerBlockFluidBase implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        final boolean dSurround = transformedName.endsWith("l");
        return transformMethod(basicClass, method -> dSurround ? method.name.equals("getColorForLiquid") : method.name.equals("getFogColor"), (method, insn) -> {
            /*
             * Old code: (changes are around line 849)
             * int color = this.getFluid().getColor();
             *
             * New code:
             * // Don't render white fog for fluids without a color.
             * int color = Hooks.getTrueColor(this.getFluid().getColor(), state, world, pos);
             */
            if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("getColor")) {
                method.instructions.insert(insn, genHookMethod("getTrueColor", "(ILnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I"));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, dSurround ? 0 : 1));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, dSurround ? 1 : 3));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @SideOnly(Side.CLIENT)
        public static int getTrueColor(final int color, @Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos) {
            return (color & 0xFFFFFF) == 0xFFFFFF ? Minecraft.getMinecraft().getBlockColors().getColor(state, world, pos) : color;
        }
    }
}
