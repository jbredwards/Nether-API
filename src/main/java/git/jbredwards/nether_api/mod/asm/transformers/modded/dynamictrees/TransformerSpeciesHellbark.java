/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
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
