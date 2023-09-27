/*
 * Copyright (c) 2023. jbredwards
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
 * Fix cascading world gen problems with Journey Into The Light
 * @author jbred
 *
 */
public final class TransformerJITLCascadingFix implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.journey.dimension.base.WorldGenJourney".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals("generateNether")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * generateNether:
                         * Old code:
                         * x = chunkX + r.nextInt(16) + 8;
                         * z = chunkZ + r.nextInt(16) + 8;
                         * ((WorldGenSingleBlock)ANCIENT_BLOCK_GEN.getValue()).generate(w, r, new BlockPos(x, y, z));
                         *
                         * New code:
                         * // Remove bad offset (the WorldGenerator class this uses already has a built-in offset)
                         * x = chunkX;
                         * z = chunkZ;
                         * ((WorldGenSingleBlock)ANCIENT_BLOCK_GEN.getValue()).generate(w, r, new BlockPos(x, y, z));
                         */
                        if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("ANCIENT_BLOCK_GEN")) {
                            for(int i = 0; i < 2; i++) {
                                AbstractInsnNode target = insn.getPrevious();
                                if(i == 1) target = target.getPrevious().getPrevious();

                                do if(target.getOpcode() == ISTORE) target = target.getPrevious();
                                else {
                                    final AbstractInsnNode prev = target.getPrevious();
                                    method.instructions.remove(target);
                                    target = prev;
                                }

                                while(!(target.getOpcode() == ALOAD && ((VarInsnNode)target).var == 2));
                                method.instructions.remove(target);
                            }
                        }
                        /*
                         * generateNether:
                         * Old code:
                         * x = chunkX + r.nextInt(16) + 8;
                         * z = chunkZ + r.nextInt(16) + 8;
                         * if (y > 20 && y < 110 && (this.isBlockTop(x, y, z, Blocks.NETHERRACK, w) || this.isBlockTop(x, y, z, JourneyBlocks.heatSand, w))) {
                         *     (new WorldGenGhastTower()).generate(w, r, new BlockPos(x, y, z));
                         * }
                         *
                         * New code:
                         * // Don't offset the structure outside loaded chunks
                         * x = chunkX + r.nextInt(10) + 8;
                         * z = chunkZ + r.nextInt(10) + 8;
                         * if (y > 20 && y < 110 && (this.isBlockTop(x, y, z, Blocks.NETHERRACK, w) || this.isBlockTop(x, y, z, JourneyBlocks.heatSand, w))) {
                         *     (new WorldGenGhastTower()).generate(w, r, new BlockPos(x, y, z));
                         * }
                         */
                        else if(insn.getOpcode() == NEW && ((TypeInsnNode)insn).desc.equals("net/journey/dimension/nether/gen/dungeon/WorldGenGhastTower")) {
                            for(int i = 0; i < 2; i++) {
                                AbstractInsnNode target = insn.getPrevious();
                                while(!(target.getOpcode() == BIPUSH && ((IntInsnNode)target).operand != 16)) target = target.getPrevious();
                                ((IntInsnNode)target).operand = 10;
                            }

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
