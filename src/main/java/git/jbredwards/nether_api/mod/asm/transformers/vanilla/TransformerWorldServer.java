/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Ensure that server-side particle spawning is not fixed by mods like Random Patches, RLMixins, and Universal Tweaks. Nether API
 * also fixes <a href="https://web.archive.org/web/20240229143258/https://bugs.mojang.com/browse/MC-10369">MC-10369</a>, but by
 * individually fixing each bug mentioned in the original report. See {@link Transformer_MC_10369}.
 * <p>
 * Applying a global fix causes bugs with many mods, like causing particles to be spawned twice.
 * </p>
 *
 * @author jbred
 *
 */
public final class TransformerWorldServer implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            createEmptyMethod(classNode, DEOBFUSCATED ? "spawnAlwaysVisibleParticle" : "func_190523_a", "(IDDDDDD[I)V");
            createEmptyMethod(classNode, DEOBFUSCATED ? "spawnParticle" : "func_175688_a", "(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V");
        });
    }

    private static void createEmptyMethod(@Nonnull final ClassNode classNode, @Nonnull final String name, @Nonnull final String desc) {
        classNode.methods.removeIf(method -> method.name.equals(name) && method.desc.equals(desc));
        @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
        method.visitInsn(RETURN);
        classNode.methods.add(method);
    }
}
