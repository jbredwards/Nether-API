/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
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
        // WorldGenJourney
        if("net.journey.dimension.base.WorldGenJourney".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals("generateNether")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                @Nonnull AbstractInsnNode target = insn.getPrevious();
                                if(i == 1) target = target.getPrevious().getPrevious();

                                do if(target.getOpcode() == ISTORE) target = target.getPrevious();
                                else {
                                    @Nonnull final AbstractInsnNode prev = target.getPrevious();
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
                                @Nonnull AbstractInsnNode target = insn.getPrevious();
                                while(!(target.getOpcode() == BIPUSH && ((IntInsnNode)target).operand != 16)) target = target.getPrevious();
                                ((IntInsnNode)target).operand = 10;
                            }

                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        // WorldGenAPI
        else if("net.slayer.api.worldgen.WorldGenAPI".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            for(@Nonnull final MethodNode method : classNode.methods) {
                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                    if(insn instanceof MethodInsnNode) {
                        // world.setBlockState(pos, state) -> world.setBlockState(pos, state, 18)
                        // world.setBlockState(pos, state, 2) -> world.setBlockState(pos, state, 18)
                        if(FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                            if("setBlockState".equals(((MethodInsnNode)insn).name)) {
                                if("(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z".equals(((MethodInsnNode)insn).desc)) method.instructions.remove(insn.getPrevious());
                                else ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                                method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));
                            }
                        }

                        // world.setBlockState(pos, state) -> world.setBlockState(pos, state, 18)
                        else if("func_175656_a".equals(((MethodInsnNode)insn).name)) {
                            method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));
                            ((MethodInsnNode)insn).name = "func_180501_a";
                            ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                        }

                        // world.setBlockState(pos, state, 2) -> world.setBlockState(pos, state, 18)
                        else if("func_180501_a".equals(((MethodInsnNode)insn).name)) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));
                        }
                    }
                }
            }

            //writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
