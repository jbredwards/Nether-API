/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import javax.annotation.Nonnull;

/**
 * Change Biomes O' Plenty's nether biome super classes
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyBiomes implements IClassTransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.startsWith("biomesoplenty")) {
            final ClassReader reader = new ClassReader(basicClass);
            if("biomesoplenty/common/biome/nether/BOPHellBiome".equals(reader.getSuperName())) {
                final ClassWriter writer = new ClassWriter(0);
                reader.accept(new ClassVisitor(Opcodes.ASM5, writer) {
                    @Override
                    public void visit(int version, int access, @Nonnull String name, @Nonnull String signature, @Nonnull String superName, @Nonnull String[] interfaces) {
                        super.visit(version, access, name, signature, "git/jbredwards/nether_api/mod/common/compat/biomesoplenty/AbstractNetherBOPBiome", interfaces);
                    }

                    @Nonnull
                    @Override
                    public MethodVisitor visitMethod(int access, @Nonnull String name, @Nonnull String desc, @Nonnull String signature, @Nonnull String[] exceptions) {
                        final MethodVisitor old = super.visitMethod(access, name, desc, signature, exceptions);
                        return "<init>".equals(name) ? new MethodVisitor(Opcodes.ASM5, old) {
                            @Override
                            public void visitMethodInsn(int opcode, @Nonnull String owner, @Nonnull String name, @Nonnull String desc, boolean itf) {
                                super.visitMethodInsn(opcode, "biomesoplenty/common/biome/nether/BOPHellBiome".equals(owner) ? "git/jbredwards/nether_api/mod/common/compat/biomesoplenty/AbstractNetherBOPBiome" : owner, name, desc, itf);
                            }
                        } : old;
                    }
                }, 0);

                return writer.toByteArray();
            }
        }

        return basicClass;
    }
}
