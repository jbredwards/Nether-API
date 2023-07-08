/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Prevent BetterNether from resetting its enabled biomes config cache
 * @author jbred
 *
 */
public final class TransformerBetterNetherConfigLoader implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("paulevs.betternether.config.ConfigLoader")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals("dispose")) {
                    /*
                     * dispose:
                     * Old code:
                     * enabledBiomes = null;
                     *
                     * New code:
                     * //keep this cached, so this mod can refer to it to know which biomes are enabled
                     * --------------------;
                     */
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == PUTSTATIC && (((FieldInsnNode)insn).name.equals("enabledBiomes") || ((FieldInsnNode)insn).name.equals("registerBiomes"))) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn);
                            break methods;
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
