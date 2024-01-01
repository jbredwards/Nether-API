/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Disable NetherEx's nether override
 * @author jbred
 *
 */
public final class TransformerNetherExOverride implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // NetherEx
        if("logictechcorp.netherex.NetherEx".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            classNode.methods.removeIf(method -> method.name.equals("onFMLServerStarting")); // remove nether world provider override
            for(@Nonnull final MethodNode method : classNode.methods) {
                /*
                 * onFMLInitialization & onFMLPostInitialization:
                 * Old code:
                 * if(NetherExConfig.dimension.nether.overrideNether)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Always properly register NetherEx's biomes
                 * if(true)
                 * {
                 *     ...
                 * }
                 */
                if(method.name.equals("onFMLInitialization") || method.name.equals("onFMLPostInitialization")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("overrideNether")) {
                            method.instructions.insert(insn, new InsnNode(ICONST_1));
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn);
                            break;
                        }
                    }
                }
            }

            // writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        // BiomeTraitGenerationHandler & BiomeDataManagerNetherEx
        else if("logictechcorp.netherex.handler.BiomeTraitGenerationHandler".equals(transformedName) || "logictechcorp.netherex.world.biome.data.BiomeDataManagerNetherEx".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                /*
                 * generateBiomeTraits | onWorldLoad:
                 * Old code:
                 * if(NetherExConfig.dimension.nether.overrideNether)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Always properly register NetherEx's biomes
                 * if(true)
                 * {
                 *     ...
                 * }
                 */
                if(method.name.equals("generateBiomeTraits") || method.name.equals("onWorldLoad")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("overrideNether")) {
                            method.instructions.insert(insn, new InsnNode(ICONST_1));
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn);
                            break methods;
                        }
                    }
                }
            }

            // writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
