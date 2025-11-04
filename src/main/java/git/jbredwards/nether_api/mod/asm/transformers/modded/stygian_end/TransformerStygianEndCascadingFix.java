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

package git.jbredwards.nether_api.mod.asm.transformers.modded.stygian_end;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import io.netty.util.internal.IntegerHolder;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Fix cascading world gen problems with Stygian End
 * @author jbred
 *
 */
public final class TransformerStygianEndCascadingFix implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // WorldGenEnderCanopy
        switch(transformedName) {
            case "fluke.stygian.world.feature.WorldGenEnderCanopy": {
                return NetherAPIConfig.StygianEnd.wideEnderCanopyGen ? basicClass : transform(basicClass, classNode -> {
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
                                final int trunkRadiusVar = getLocalVar(method, "trunkRadius");
                                final int canopyRadiusVar = getLocalVar(method, "canopyRadius");
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
                                final int branchLengthVar = getLocalVar(method, "branchLength"); // dynamically find the variable index, since stygian end continuation changes it
                                if(branchLengthVar == -1) throw new UnsupportedOperationException("Unsupported version of Stygian End found, please try a different version!");
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
                                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_175656_a")) {
                                        method.instructions.insertBefore(insn, genBlockFlags());

                                        if(DEOBFUSCATED) ((MethodInsnNode)insn).name = "func_180501_a";
                                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                });
            }

            // WorldGenEndVolcano
            case "fluke.stygian.world.feature.WorldGenEndVolcano": {
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
                @Nonnull final IntegerHolder changes = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "generate" : "func_180709_b"), (method, insn) -> {
                    // int radius = 9 + rand.nextInt(6) -> int radius = 9 + rand.nextInt(5)
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 6) {
                        method.instructions.insert(insn, new InsnNode(ICONST_5));
                        method.instructions.remove(insn);
                    }
                    // world.setBlockState(baseBlock, volcBlock) -> world.setBlockState(baseBlock, volcBlock, 18)
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_175656_a")) {
                        method.instructions.insertBefore(insn, genBlockFlags());

                        if(!DEOBFUSCATED) ((MethodInsnNode)insn).name = "func_180501_a";
                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                        if(++changes.value == 2) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // BiomeEndVolcano
            case "fluke.stygian.world.biomes.BiomeEndVolcano": {
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
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "decorate" : "func_180624_a"), (method, insn) -> {
                    // this.getEndSurfaceHeight(world, ..., (IBlockState)null) -> this.getEndSurfaceHeight(world, ..., END_OBSIDIAN)
                    if(insn.getOpcode() == ACONST_NULL) {
                        method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "fluke/stygian/world/biomes/BiomeEndVolcano", "END_OBSIDIAN", "Lnet/minecraft/block/state/IBlockState;"));
                        method.instructions.remove(insn);
                    }
                    // world.setBlockState(p, ModBlocks.endAcid.getDefaultState()) -> world.setBlockState(p, ModBlocks.endAcid.getDefaultState(), 18)
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_175656_a")) {
                        method.instructions.insertBefore(insn, genBlockFlags());

                        if(!DEOBFUSCATED) ((MethodInsnNode)insn).name = "func_180501_a";
                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
        }

        return basicClass;
    }
}
