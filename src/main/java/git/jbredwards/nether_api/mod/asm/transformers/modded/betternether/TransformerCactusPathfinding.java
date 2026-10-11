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
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Fix BetterNether cacti not having the correct entity path node type
 * @author jbred
 *
 */
public final class TransformerCactusPathfinding implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> transformOverwrite(classNode,
                new MethodNode(ACC_PUBLIC, "getAiPathNodeType", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/pathfinding/PathNodeType;", null, null),
                generator -> generator.visitFieldInsn(GETSTATIC, "net/minecraft/pathfinding/PathNodeType", "DAMAGE_CACTUS", "Lnet/minecraft/pathfinding/PathNodeType;")));
    }
}
