/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.height;

import net.minecraft.init.Biomes;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_Natura implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // GlowshroomGenerator
        if("com.progwml6.natura.world.worldgen.GlowshroomGenerator".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generateNether")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generateNether:
                         * Old code:
                         * if (BiomeDictionary.hasType(biome, Type.NETHER))
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                         * if (Hooks.VALID_GLOWSHROOM_BIOMES.contains(biome))
                         * {
                         *     ...
                         * }
                         */
                        if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.insertBefore(insn.getPrevious(), new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/height/Transformer_NetherHeight_Natura$Hooks", "VALID_GLOWSHROOM_BIOMES", "Ljava/util/Set;"));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true));
                            method.instructions.remove(insn);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = this.findGround(world, xSpawn, random.nextInt(64) + 32, zSpawn);
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = this.findGround(world, xSpawn, random.nextInt(64 + (world.getActualHeight() >> 8 << 7)) + 32, zSpawn);
                         */
                        else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 64) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new IntInsnNode(BIPUSH, 7));
                            list.add(new InsnNode(ISHL));
                            list.add(new InsnNode(IADD));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = random.nextInt(128);
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = random.nextInt(world.getActualHeight());
                         */
                        else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.remove(insn);
                        }
                    }
                }
            }

            // writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        // NetherTreesGenerator
        else if("com.progwml6.natura.world.worldgen.NetherTreesGenerator".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generateNether")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generateNether:
                         * Old code:
                         * if (BiomeDictionary.hasType(biome, Type.NETHER))
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                         * if (Hooks.VALID_TREE_BIOMES.contains(biome))
                         * {
                         *     ...
                         * }
                         */
                        if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.insertBefore(insn.getPrevious(), new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/height/Transformer_NetherHeight_Natura$Hooks", "VALID_TREE_BIOMES", "Ljava/util/Set;"));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true));
                            method.instructions.remove(insn);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = 72;
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = 72 + (world.getActualHeight() >> 8 << 7);
                         */
                        else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 72) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new IntInsnNode(BIPUSH, 7));
                            list.add(new InsnNode(ISHL));
                            list.add(new InsnNode(IADD));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = random.nextInt(64) + 32;
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = random.nextInt(64 + (world.getActualHeight() >> 8 << 7)) + 32;
                         */
                        else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 64) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new IntInsnNode(BIPUSH, 7));
                            list.add(new InsnNode(ISHL));
                            list.add(new InsnNode(IADD));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = random.nextInt(80) + 16;
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = random.nextInt(80 + (world.getActualHeight() >> 8 << 7)) + 16;
                         */
                        else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 80) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 4));
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
            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        // VineGenerator
        else if("com.progwml6.natura.world.worldgen.VineGenerator".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generateNether")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generateNether:
                         * Old code:
                         * if (BiomeDictionary.hasType(biome, Type.NETHER))
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                         * if (Hooks.VALID_VINE_BIOMES.contains(biome))
                         * {
                         *     ...
                         * }
                         */
                        if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.insertBefore(insn.getPrevious(), new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/height/Transformer_NetherHeight_Natura$Hooks", "VALID_VINE_BIOMES", "Ljava/util/Set;"));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true));
                            method.instructions.remove(insn);
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * ySpawn = 108;
                         *
                         * New code:
                         * // Use actual nether height instead of a hardcoded value
                         * ySpawn = 108 + (world.getActualHeight() >> 8 << 7) + 5;
                         */
                        else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 108) {
                            @Nonnull final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new IntInsnNode(BIPUSH, 7));
                            list.add(new InsnNode(ISHL));
                            list.add(new InsnNode(IADD));
                            list.add(new InsnNode(ICONST_5));
                            list.add(new InsnNode(IADD));
                            method.instructions.insert(insn, list);
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

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static final Set<Biome> // mutable in case pack devs want to add any biomes
                VALID_GLOWSHROOM_BIOMES = new HashSet<>(Arrays.asList(Biomes.HELL)),
                VALID_TREE_BIOMES = new HashSet<>(Arrays.asList(Biomes.HELL)),
                VALID_VINE_BIOMES = new HashSet<>(Arrays.asList(Biomes.HELL));
    }
}
