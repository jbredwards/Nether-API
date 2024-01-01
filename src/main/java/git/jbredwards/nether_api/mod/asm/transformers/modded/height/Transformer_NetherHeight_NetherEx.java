/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.height;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_NetherEx implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // BiomeTraitGenerationHandler
        if("logictechcorp.netherex.handler.BiomeTraitGenerationHandler".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generateBiomeTraits")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generateTerrain:
                         * Old code:
                         * for(int generationAttempts = 0; generationAttempts < trait.getGenerationAttempts(world, pos, random); generationAttempts++)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Double gen attempts to account for world height space
                         * for(int generationAttempts = 0; generationAttempts < trait.getGenerationAttempts(world, pos, random) << (world.getActualHeight() >> 8); generationAttempts++)
                         * {
                         *     ...
                         * }
                         */
                        /*if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("getGenerationAttempts")) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new InsnNode(ISHL));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * generateTerrain:
                         * Old code:
                         * trait.generate(world, pos.add(random.nextInt(16) + 8, RandomHelper.getNumberInRange(trait.getMinimumGenerationHeight(world, pos, random), trait.getMaximumGenerationHeight(world, pos, random), random), random.nextInt(16) + 8), random);
                         *
                         * New code:
                         * // Double gen height to account for world height space
                         * trait.generate(world, pos.add(random.nextInt(16) + 8, RandomHelper.getNumberInRange(trait.getMinimumGenerationHeight(world, pos, random), trait.getMaximumGenerationHeight(world, pos, random), random) + (world.getActualHeight() >> 8 << 7), random.nextInt(16) + 8), random);
                         */
                        /*else*/ if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("getMaximumGenerationHeight")) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new IntInsnNode(BIPUSH, 7));
                            list.add(new InsnNode(ISHL));
                            list.add(new InsnNode(IADD));
                            method.instructions.insert(insn, list);
                            break methods;
                        }
                    }
                }
            }

            // writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        // BiomeDataNetherEx
        else if("logictechcorp.netherex.world.biome.data.BiomeDataNetherEx".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                /*
                 * generateTerrain:
                 * Old code:
                 * for (int posY = 127; posY >= 0; posY--)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Use actual nether height instead of a hardcoded value
                 * for (int posY = world.getActualHeight() - 1; posY >= 0; posY--)
                 * {
                 *     ...
                 * }
                 */
                if(method.name.equals("generateTerrain")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 127) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                            method.instructions.insertBefore(insn, new InsnNode(ISUB));
                            method.instructions.remove(insn);
                            break methods;
                        }
                    }
                }
            }

            // writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
