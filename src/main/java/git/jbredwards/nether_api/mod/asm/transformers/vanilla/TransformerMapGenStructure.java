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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Add accessor for certain methods.
 * @author jbred
 *
 */
public final class TransformerMapGenStructure implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            classNode.interfaces.add("git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerMapGenStructure$Accessor");
            /*
             * New code:
             * // Accessor.
             * @ASMGenerated
             * public StructureStart nether_api$getStructureAt(BlockPos pos)
             * {
             *     this.getStructureAt(pos);
             * }
             */
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "nether_api$getStructureAt", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/gen/structure/StructureStart;", null, null), generator -> {
                generator.loadThis();
                generator.loadArg(0);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/gen/structure/MapGenStructure", DEOBFUSCATED ? "getStructureAt" : "func_175797_c", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/gen/structure/StructureStart;", false);
            });
            /*
             * New code:
             * // Accessor.
             * @ASMGenerated
             * public void nether_api$initializeStructureData(World world)
             * {
             *     this.initializeStructureData(world);
             * }
             */
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "nether_api$initializeStructureData", "(Lnet/minecraft/world/World;)V", null, null), generator -> {
                generator.loadThis();
                generator.loadArg(0);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/gen/structure/MapGenStructure", DEOBFUSCATED ? "initializeStructureData" : "func_143027_a", "(Lnet/minecraft/world/World;)V", false);
            });
        });
    }

    public interface Accessor
    {
        @Nullable
        StructureStart nether_api$getStructureAt(@Nonnull final BlockPos pos);
        void nether_api$initializeStructureData(@Nonnull final World world);
    }
}
