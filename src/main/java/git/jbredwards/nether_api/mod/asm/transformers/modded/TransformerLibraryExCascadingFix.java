/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Fix cascading world gen problems with LibraryEx<p>
 * Note that some (like the 32x32 NetherEx villages) naturally can't be fixed due to being larger than 16x16
 * @author jbred
 *
 */
public final class TransformerLibraryExCascadingFix implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("logictechcorp.libraryex.utility.StructureHelper")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            for(final MethodNode method : classNode.methods) {
                for(final AbstractInsnNode insn : method.instructions.toArray()) {
                    /*
                     * Old code:
                     * float sizeX = structureSize.getX() + 2;
                     * float sizeY = structureSize.getY() + 1;
                     * float sizeZ = structureSize.getZ() + 2;
                     *
                     * New code:
                     * //fix bad structure size
                     * float sizeX = structureSize.getX();
                     * float sizeY = structureSize.getY() + 1;
                     * float sizeZ = structureSize.getZ();
                     */
                    if(insn.getOpcode() == ICONST_2 && insn.getNext().getOpcode() == IADD) {
                        method.instructions.remove(insn.getNext());
                        method.instructions.remove(insn);
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
