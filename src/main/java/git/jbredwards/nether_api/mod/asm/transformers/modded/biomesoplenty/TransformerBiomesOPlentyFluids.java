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
         *     return !NetherAPIConfig.BOP.placeableNetherFluids;
         * }
         */
        return transform(basicClass, classNode -> {
            if(classNode.methods.stream().anyMatch(method -> method.name.equals("doesVaporize"))) return; // Just in case another mod adds this fix...
            @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "doesVaporize", "(Lnet/minecraftforge/fluids/FluidStack;)Z", null, null);
            classNode.methods.add(method);

            @Nonnull final GeneratorAdapter adapter = new GeneratorAdapter(method, method.access, method.name, method.desc);
            adapter.visitFieldInsn(GETSTATIC, "git/jbredwards/nether_api/mod/common/config/NetherAPIConfig$BOP", "placeableNetherFluids", "Z");
            adapter.visitInsn(ICONST_1);
            adapter.visitInsn(IXOR);
            adapter.returnValue();
        });
    }
}
