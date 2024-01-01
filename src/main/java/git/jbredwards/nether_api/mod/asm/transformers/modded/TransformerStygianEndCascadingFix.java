/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Fix cascading world gen problems with Stygian End
 * @author jbred
 *
 */
public final class TransformerStygianEndCascadingFix implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // WorldGenEnderCanopy
        switch(transformedName) {
            case "fluke.stygian.world.feature.WorldGenEnderCanopy":
                if(!NetherAPIConfig.StygianEnd.wideEnderCanopyGen) {
                    @Nonnull final ClassNode classNode = new ClassNode();
                    new ClassReader(basicClass).accept(classNode, 0);
                    for(@Nonnull final MethodNode method : classNode.methods) {
                        switch(method.name) {
                            /*
                             * isValidGenLocation:
                             * Old code:
                             * for (BlockPos canopyBlock : BlockPos.getAllInBoxMutable(pos.add(-23, trunkHeight + 7, -23), pos.add(23, trunkHeight + 7, 23)))
                             * {
                             *     ...
                             * }
                             *
                             * New code:
                             * // Don't check blocks outside unloaded chunks
                             * for (BlockPos canopyBlock : BlockPos.getAllInBoxMutable(pos.add(-15, trunkHeight + 7, -15), pos.add(15, trunkHeight + 7, 15)))
                             * {
                             *     ...
                             * }
                             */
                            case "isValidGenLocation":
                                // vars for stygian end continuation only
                                final int trunkRadiusVar = method.localVariables.stream().filter(var -> var.name.equals("trunkRadius")).mapToInt(var -> var.index).findFirst().orElse(-1);
                                final int canopyRadiusVar = method.localVariables.stream().filter(var -> var.name.equals("canopyRadius")).mapToInt(var -> var.index).findFirst().orElse(-1);
                                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                                    if(insn.getOpcode() == BIPUSH) {
                                        if(((IntInsnNode)insn).operand == -23) ((IntInsnNode)insn).operand = -15;
                                        else if(((IntInsnNode)insn).operand == 23) ((IntInsnNode)insn).operand = 15;
                                    }

                                    // fix REALLY cringe stygian end continuation code
                                    else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 529) ((IntInsnNode)insn).operand = 225;
                                    else if(insn.getOpcode() == ISTORE && (((VarInsnNode)insn).var == trunkRadiusVar || ((VarInsnNode)insn).var == canopyRadiusVar)) {
                                        method.instructions.remove(insn.getPrevious());
                                        method.instructions.insertBefore(insn, new InsnNode(ICONST_M1));
                                    }
                                }
                                break;

                            /*
                             * buildCanopy:
                             * Old code:
                             * int canopyRadius = 8;
                             *
                             * New code:
                             * // Reduce leaf radius to both: prevent cascading world gen, and prevent leaves from decaying due to being too far from a log
                             * int canopyRadius = 7;
                             */
                            case "buildCanopy":
                                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 8) {
                                        ((IntInsnNode)insn).operand = 7;
                                        break;
                                    }
                                }
                                break;

                            /*
                             * buildTrunk:
                             * Old code:
                             * colHeight = 18 - Math.abs(x) - Math.abs(z) * 3 - rand.nextInt(2);
                             * ...
                             * colHeight = 18 - Math.abs(x) * 3 - Math.abs(z) - rand.nextInt(2);
                             *
                             * New code:
                             * // Reduce tree radius, so the tree looks better with a smaller branch spread
                             * colHeight = 15 - Math.abs(x) - Math.abs(z) * 3 - rand.nextInt(2);
                             * ...
                             * colHeight = 15 - Math.abs(x) * 3 - Math.abs(z) - rand.nextInt(2);
                             */
                            case "buildTrunk":
                                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 18) {
                                        ((IntInsnNode)insn).operand = 15;
                                    }
                                }
                                break;

                            /*
                             * buildBranches:
                             * Old code:
                             * branchLength = 14 + rand.nextInt(8);
                             * ...
                             * branchLength = 9 + rand.nextInt(7);
                             * ...
                             * branchLength = 5 + rand.nextInt(5);
                             *
                             * New code:
                             * // Greatly reduce branch horizontal lengths, as to not generate outside the 2x2 area of chunks
                             * branchLength = 4 + rand.nextInt(4);
                             * ...
                             * branchLength = 4 + rand.nextInt(3);
                             * ...
                             * branchLength = 2 + rand.nextInt(2);
                             */
                            case "buildBranches":
                                final int branchLengthVar = method.localVariables.stream() // dynamically find the variable index, since stygian end continuation changes it
                                        .filter(var -> var.name.equals("branchLength"))
                                        .mapToInt(var -> var.index)
                                        .findFirst()
                                        .orElseThrow(() -> new UnsupportedOperationException("Unsupported version of Stygian End found, please try a different version!"));
                                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                                    if(insn.getOpcode() == ISTORE && ((VarInsnNode)insn).var == branchLengthVar) {
                                        @Nonnull AbstractInsnNode nextBackInsn = insn.getPrevious(), backInsn;
                                        for(int maxChanges = 2; maxChanges > 0;) {
                                            backInsn = nextBackInsn;
                                            nextBackInsn = backInsn.getPrevious();

                                            if(backInsn instanceof IntInsnNode) {
                                                maxChanges--;
                                                switch(((IntInsnNode)backInsn).operand) {
                                                    case 14:
                                                    case 8:
                                                    case 9:
                                                        method.instructions.insert(backInsn, new InsnNode(ICONST_4));
                                                        method.instructions.remove(backInsn);
                                                        break;
                                                    case 7:
                                                        method.instructions.insert(backInsn, new InsnNode(ICONST_3));
                                                        method.instructions.remove(backInsn);
                                                        break;
                                                }
                                            } else if(backInsn.getOpcode() == ICONST_5) {
                                                maxChanges--;
                                                method.instructions.insert(backInsn, new InsnNode(ICONST_2));
                                                method.instructions.remove(backInsn);
                                            }
                                        }
                                    }
                                    // fix more weird stygian end continuation jank
                                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("isValidGenLocation")) {
                                        method.instructions.insert(insn, new InsnNode(ICONST_1));
                                        method.instructions.remove(insn.getPrevious());
                                        method.instructions.remove(insn.getPrevious());
                                        method.instructions.remove(insn.getPrevious());
                                        method.instructions.remove(insn.getPrevious());
                                        method.instructions.remove(insn);
                                    }
                                }
                                break;

                            /*
                             * placeLogAt & placeLeafAt:
                             * Old code:
                             * worldIn.setBlockState(pos, LOG);
                             * ...
                             * worldIn.setBlockState(pos, LEAF);
                             *
                             * New code:
                             * // Don't use bad block flags for tree generation
                             * // This exists for compatibility with the continuation fork, and is not needed for other versions
                             * worldIn.setBlockState(pos, LOG, 18);
                             * ...
                             * worldIn.setBlockState(pos, LEAF, 18);
                             */
                            case "placeLogAt":
                            case "placeLeafAt":
                                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_175656_a")) {
                                        method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));

                                        if(!FMLLaunchHandler.isDeobfuscatedEnvironment()) ((MethodInsnNode)insn).name = "func_180501_a";
                                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                                        break;
                                    }
                                }
                                break;
                        }
                    }

                    //writes the changes
                    @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    classNode.accept(writer);
                    return writer.toByteArray();
                }
                break;
            // WorldGenEndVolcano
            case "fluke.stygian.world.feature.WorldGenEndVolcano": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for (@Nonnull final MethodNode method : classNode.methods) {
                    /*
                     * generate:
                     * Old code:
                     * int radius = 9 + rand.nextInt(6);
                     * ...
                     * world.setBlockState(baseBlock, volcBlock);
                     * ...
                     * world.setBlockState(pos.add(x, y, z), volcBlock);
                     *
                     * New code:
                     * // Shrink max radius by one block, as to not generate into unloaded chunks
                     * int radius = 9 + rand.nextInt(5);
                     * ...
                     * // Don't use bad block flags for volcano generation
                     * world.setBlockState(baseBlock, volcBlock, 18);
                     * ...
                     * world.setBlockState(pos.add(x, y, z), volcBlock, 18);
                     */
                    if (method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "generate" : "func_180709_b")) {
                        int changes = 0;
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            // int radius = 9 + rand.nextInt(6) -> int radius = 9 + rand.nextInt(5)
                            if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 6) {
                                method.instructions.insert(insn, new InsnNode(ICONST_5));
                                method.instructions.remove(insn);
                            }
                            // world.setBlockState(baseBlock, volcBlock) -> world.setBlockState(baseBlock, volcBlock, 18)
                            else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_175656_a")) {
                                method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));

                                if(!FMLLaunchHandler.isDeobfuscatedEnvironment()) ((MethodInsnNode)insn).name = "func_180501_a";
                                ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                                if(++changes == 2) break methods;
                            }
                        }
                    }
                }

                //writes the changes
                @Nonnull final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }
            // BiomeEndVolcano
            case "fluke.stygian.world.biomes.BiomeEndVolcano": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    /*
                     * generate:
                     * Old code:
                     * this.getEndSurfaceHeight(world, ..., (IBlockState)null);
                     * ...
                     * world.setBlockState(p, ModBlocks.endAcid.getDefaultState());
                     *
                     * New code:
                     * // Don't generate volcano features outside the volcano biome
                     * this.getEndSurfaceHeight(world, ..., END_OBSIDIAN);
                     * ...
                     * // Don't use bad block flags for acid generation
                     * world.setBlockState(p, ModBlocks.endAcid.getDefaultState(), 18);
                     */
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "decorate" : "func_180624_a")) {
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            // this.getEndSurfaceHeight(world, ..., (IBlockState)null) -> this.getEndSurfaceHeight(world, ..., END_OBSIDIAN)
                            if(insn.getOpcode() == ACONST_NULL) {
                                method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "fluke/stygian/world/biomes/BiomeEndVolcano", "END_OBSIDIAN", "Lnet/minecraft/block/state/IBlockState;"));
                                method.instructions.remove(insn);
                            }
                            // world.setBlockState(p, ModBlocks.endAcid.getDefaultState()) -> world.setBlockState(p, ModBlocks.endAcid.getDefaultState(), 18)
                            else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_175656_a")) {
                                method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));

                                if(!FMLLaunchHandler.isDeobfuscatedEnvironment()) ((MethodInsnNode)insn).name = "func_180501_a";
                                ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
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
        }

        return basicClass;
    }
}
