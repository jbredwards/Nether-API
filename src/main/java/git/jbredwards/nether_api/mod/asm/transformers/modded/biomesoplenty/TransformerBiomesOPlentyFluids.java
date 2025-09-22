/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Prevent certain BOP fluids from vaporizing.
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyFluids implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        /*
         * New code:
         * // Prevent certain BOP fluids from vaporizing.
         * @ASMGenerated
         * public boolean doesVaporize(FluidStack fluidStack)
         * {
         *     return false;
         * }
         */
        return transform(basicClass, classNode -> {
            if(classNode.methods.stream().anyMatch(method -> method.name.equals("doesVaporize"))) return; // Just in case another mod adds this fix...
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "doesVaporize", "(Lnet/minecraftforge/fluids/FluidStack;)Z", null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            adapter.visitInsn(ICONST_0);
            adapter.returnValue();
        });
    }
}
