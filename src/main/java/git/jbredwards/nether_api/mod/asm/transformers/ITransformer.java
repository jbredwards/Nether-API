/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
@FunctionalInterface
public interface ITransformer extends IClassTransformer, Opcodes
{
    boolean DEOBFUSCATED = FMLLaunchHandler.isDeobfuscatedEnvironment();
    enum BreakType { CONTINUE, INSTRUCTIONS, METHODS }

    default int getLocalVar(@Nonnull final MethodNode method, @Nonnull final String name) {
        return method.localVariables == null ? -1 : method.localVariables.stream().filter(lv -> name.equals(lv.name)).mapToInt(lv -> lv.index).findFirst().orElse(-1);
    }

    @Nonnull
    default byte[] transform(@Nonnull final byte[] basicClass, @Nonnull final Consumer<ClassNode> action) {
        return transform(basicClass, false, action);
    }

    @Nonnull
    default byte[] transform(@Nonnull final byte[] basicClass, final boolean recalcFrames, @Nonnull final Consumer<ClassNode> action) {
        @Nonnull final ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, recalcFrames ? ClassReader.SKIP_FRAMES : 0);
        action.accept(classNode);

        // writes the changes
        @Nonnull final ClassWriter writer = new ClassWriter((recalcFrames ? ClassWriter.COMPUTE_FRAMES : 0) | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    @Nonnull
    default byte[] transformMethod(@Nonnull final byte[] basicClass, final boolean recalcFrames, @Nonnull final Predicate<MethodNode> condition, @Nonnull final BiFunction<MethodNode, AbstractInsnNode, BreakType> action) {
        return transform(basicClass, recalcFrames, classNode -> transformMethod(classNode, condition, action));
    }

    @Nonnull
    default byte[] transformMethod(@Nonnull final byte[] basicClass, @Nonnull final Predicate<MethodNode> condition, @Nonnull final BiFunction<MethodNode, AbstractInsnNode, BreakType> action) {
        return transformMethod(basicClass, false, condition, action);
    }

    default void transformMethod(@Nonnull final ClassNode classNode, @Nonnull final Predicate<MethodNode> condition, @Nonnull final BiFunction<MethodNode, AbstractInsnNode, BreakType> action) {
        methods: for(@Nonnull final MethodNode method : classNode.methods) if(condition.test(method)) {
            instructions: for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                switch(action.apply(method, insn)) {
                    case METHODS: break methods;
                    case INSTRUCTIONS: break instructions;
                }
            }
        }
    }

    @Nonnull
    default FieldInsnNode genHookField(@Nonnull final String name, @Nonnull final String desc) {
        return new FieldInsnNode(GETSTATIC, getClass().getName().replace('.', '/') + "$Hooks", name, desc);
    }

    @Nonnull
    default MethodInsnNode genHookMethod(@Nonnull final String name, @Nonnull final String desc) {
        return new MethodInsnNode(INVOKESTATIC, getClass().getName().replace('.', '/') + "$Hooks", name, desc, false);
    }

    // --------

    @Nonnull
    default IntInsnNode genBlockFlags() {
        return new IntInsnNode(BIPUSH, Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
    }

    @Nonnull
    default InsnList genHeightOffset(final boolean add) {
        @Nonnull final InsnList list = new InsnList();
        list.add(genHeightMethod());

        // world.getActualHeight() >> 8 << 7
        list.add(new IntInsnNode(BIPUSH, 8));
        list.add(new InsnNode(ISHR));
        list.add(new IntInsnNode(BIPUSH, 7));
        list.add(new InsnNode(ISHL));

        if(add) list.add(new InsnNode(IADD));
        return list;
    }

    @Nonnull
    default MethodInsnNode genHeightMethod() {
        return new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", DEOBFUSCATED ? "getActualHeight" : "func_72940_L", "()I", false);
    }
}
