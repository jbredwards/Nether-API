/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

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
            /*
             * Old code:
             * super(server, saveHandlerIn, new DerivedWorldInfo(delegate.getWorldInfo()), dimensionId, profilerIn);
             *
             * New code:
             * // Allow each dimension to have its own global spawn position.
             * super(server, saveHandlerIn, new DerivedWorldInfoMulti(delegate.getWorldInfo(), delegate, dimensionId), dimensionId, profilerIn);
             */
            if(transformedName.endsWith("Multi")) transformMethod(classNode, method -> method.name.equals("<init>"), (method, insn) -> {
                if(insn.getOpcode() == NEW && ((TypeInsnNode)insn).desc.equals("net/minecraft/world/storage/DerivedWorldInfo"))
                    ((TypeInsnNode)insn).desc = "git/jbredwards/nether_api/mod/common/world/DerivedWorldInfoMulti";
                else if(insn.getOpcode() == INVOKESPECIAL && ((MethodInsnNode)insn).owner.equals("net/minecraft/world/storage/DerivedWorldInfo")) {
                    ((MethodInsnNode)insn).owner = "git/jbredwards/nether_api/mod/common/world/DerivedWorldInfoMulti";
                    ((MethodInsnNode)insn).desc = "(Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/World;I)V";
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                    method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 3));
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        });
    }

    private static void createEmptyMethod(@Nonnull final ClassNode classNode, @Nonnull final String name, @Nonnull final String desc) {
        classNode.methods.removeIf(method -> method.name.equals(name) && method.desc.equals(desc));
        @Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
        method.visitInsn(RETURN);
        classNode.methods.add(method);
    }
}
