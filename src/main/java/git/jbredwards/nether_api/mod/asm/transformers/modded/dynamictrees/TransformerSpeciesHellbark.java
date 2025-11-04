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

package git.jbredwards.nether_api.mod.asm.transformers.modded.dynamictrees;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Fix some bugs with the BOP hellback dynamic trees species
 * @author jbred
 *
 */
public final class TransformerSpeciesHellbark implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(transformParent(basicClass, "com/ferreusveritas/dynamictrees/trees/Species", "git/jbredwards/nether_api/mod/common/compat/dynamictrees/SpeciesPlantable"), classNode -> {
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "getSimulatedPlantable", "()Lnet/minecraftforge/common/IPlantable;", null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            adapter.visitFieldInsn(GETSTATIC, "git/jbredwards/nether_api/api/util/PlantUtils", "NETHERRACK_PLANT_TYPE", "Lnet/minecraftforge/common/EnumPlantType;");
            adapter.visitMethodInsn(INVOKESTATIC, "git/jbredwards/nether_api/api/util/PlantUtils", "createPlantable", "(Lnet/minecraftforge/common/EnumPlantType;)Lnet/minecraftforge/common/IPlantable;", false);
            adapter.returnValue();
        });
    }
}
