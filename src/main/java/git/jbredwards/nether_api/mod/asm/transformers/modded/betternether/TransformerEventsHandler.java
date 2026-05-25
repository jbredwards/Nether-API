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
import org.objectweb.asm.AnnotationVisitor;

import javax.annotation.Nonnull;

/**
 * Run world load event before generation.
 * @author jbred
 *
 */
public final class TransformerEventsHandler implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("onWorldLoad"), (method, insn) -> {
            method.visibleAnnotations.clear();

            @Nonnull final AnnotationVisitor annotation = method.visitAnnotation("Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;", true);
            annotation.visitEnum("priority", "Lnet/minecraftforge/fml/common/eventhandler/EventPriority;", "HIGHEST");
            annotation.visitEnd();

            return BreakType.METHODS;
        });
    }
}
