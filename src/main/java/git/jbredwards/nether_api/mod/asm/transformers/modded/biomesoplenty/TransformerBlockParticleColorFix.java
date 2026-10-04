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

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import biomesoplenty.common.block.BlockBOPDoublePlant;
import git.jbredwards.nether_api.api.block.IConditionalParticleColor;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * Fix BOP grass block break particles
 * @author jbred
 *
 */
public final class TransformerBlockParticleColorFix implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> classNode.interfaces.add(
                "git/jbredwards/nether_api/mod/asm/transformers/modded/biomesoplenty/TransformerBlockParticleColorFix$Accessor"
                + classNode.name.substring("biomesoplenty.common.block.BlockBOP".length(), classNode.name.length())));
    }

    @SuppressWarnings("unused")
    public interface AccessorDoublePlant extends IConditionalParticleColor
    {
        @Override
        default boolean particleUseBlockColor(@Nonnull final IBlockState state) {
            return state.getValue(BlockBOPDoublePlant.VARIANT) == BlockBOPDoublePlant.DoublePlantType.FLAX;
        }
    }

    @SuppressWarnings("unused")
    public interface AccessorGrass extends IConditionalParticleColor
    {
        @Override
        default boolean particleUseBlockColor(@Nonnull final IBlockState state) {
            return false;
        }
    }
}
