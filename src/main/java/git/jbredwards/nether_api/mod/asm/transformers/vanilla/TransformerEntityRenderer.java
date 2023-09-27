/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Fix MC-31681 (Fog and clouds darken when indoors or under trees)
 * @author jbred
 *
 */
public final class TransformerEntityRenderer implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.minecraft.client.renderer.EntityRenderer".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "updateRenderer" : "func_78464_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * updateRenderer: (changes are around line 355)
                         * Old code:
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Fix MC-31681 (Fog and clouds darken when indoors or under trees)
                         * {
                         *     ...
                         *     this.fogColor1 = 1F;
                         * }
                         */
                        if(insn.getOpcode() == RETURN) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new InsnNode(FCONST_1));
                            method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/client/renderer/EntityRenderer", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "fogColor1" : "field_78539_ae", "F"));
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
