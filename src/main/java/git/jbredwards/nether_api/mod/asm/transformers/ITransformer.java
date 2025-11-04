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

package git.jbredwards.nether_api.mod.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
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
    default String genHookClass() {
        return getClass().getName().replace('.', '/') + "$Hooks";
    }

    @Nonnull
    default FieldInsnNode genHookField(@Nonnull final String name, @Nonnull final String desc) {
        return new FieldInsnNode(GETSTATIC, genHookClass(), name, desc);
    }

    @Nonnull
    default MethodInsnNode genHookMethod(@Nonnull final String name, @Nonnull final String desc) {
        return new MethodInsnNode(INVOKESTATIC, genHookClass(), name, desc, false);
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

    @Nonnull
    default byte[] transformParent(@Nonnull final byte[] basicClass, @Nonnull final String oldSuperName, @Nonnull final String newSuperName) {
        @Nonnull final ClassReader reader = new ClassReader(basicClass);
        if(oldSuperName.equals(reader.getSuperName())) {
            @Nonnull final ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassVisitor(ASM5, writer) {
                @Override
                public void visit(final int version, final int access, @Nonnull final String name, @Nonnull final String signature, @Nonnull final String superName, @Nonnull final String[] interfaces) {
                    super.visit(version, access, name, signature, newSuperName, interfaces);
                }

                @Nonnull
                @Override
                public MethodVisitor visitMethod(final int access, @Nonnull final String name, @Nonnull final String desc, @Nonnull final String signature, @Nonnull final String[] exceptions) {
                    @Nonnull final MethodVisitor old = super.visitMethod(access, name, desc, signature, exceptions);
                    return "<init>".equals(name) ? new MethodVisitor(api, old) {
                        @Override
                        public void visitMethodInsn(final int opcode, @Nonnull final String owner, @Nonnull final String name, @Nonnull final String desc, final boolean itf) {
                            super.visitMethodInsn(opcode, oldSuperName.equals(owner) ? newSuperName : owner, name, desc, itf);
                        }
                    } : old;
                }
            }, 0);

            return writer.toByteArray();
        }

        return basicClass;
    }

    default void transformPlantable(@Nonnull final ClassNode classNode, @Nonnull final String plantType) {
        if(!classNode.interfaces.contains("net/minecraftforge/common/IPlantable")) {
            classNode.interfaces.add("net/minecraftforge/common/IPlantable");

            @Nonnull final MethodNode plant = new MethodNode(ACC_PUBLIC, "getPlant", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", null, null);
            @Nonnull final MethodNode type = new MethodNode(ACC_PUBLIC, "getPlantType", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraftforge/common/EnumPlantType;", null, null);
            classNode.methods.removeIf(method -> method.name.equals(plant.name) || method.name.equals(type.name));
            classNode.methods.add(plant);
            classNode.methods.add(type);

            @Nonnull final GeneratorAdapter plantAdapter = new GeneratorAdapter(plant, plant.access, plant.name, plant.desc);
            plantAdapter.loadThis();
            plantAdapter.visitMethodInsn(INVOKEVIRTUAL, classNode.name, DEOBFUSCATED ? "getDefaultState" : "func_176223_P", "()Lnet/minecraft/block/state/IBlockState;", false);
            plantAdapter.returnValue();

            @Nonnull final GeneratorAdapter typeAdapter = new GeneratorAdapter(type, type.access, type.name, type.desc);
            typeAdapter.visitFieldInsn(GETSTATIC, "git/jbredwards/nether_api/api/util/PlantUtils", plantType, "Lnet/minecraftforge/common/EnumPlantType;");
            typeAdapter.returnValue();
        }
    }
}
