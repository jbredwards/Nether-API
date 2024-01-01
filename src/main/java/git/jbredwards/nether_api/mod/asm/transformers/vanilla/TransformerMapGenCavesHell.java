/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.block.INetherCarvable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Ensures that nether caves can carve through any biome
 * @author jbred
 *
 */
public final class TransformerMapGenCavesHell implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.minecraft.world.gen.MapGenCavesHell".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);
            methods:
            for(final MethodNode method : classNode.methods) {
                //transform addTunnel method
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "addTunnel" : "func_180704_a")) {
                    //ensures that the new local variable can be called anywhere in the method
                    final LabelNode start = new LabelNode();
                    final LabelNode end = new LabelNode();
                    method.instructions.insertBefore(method.instructions.getFirst(), start);
                    method.instructions.insert(method.instructions.getLast(), end);
                    //adds the new local variable
                    method.localVariables.add(new LocalVariableNode("biome", "Lnet/minecraft/world/biome/Biome;", null, start, end, 62));
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * addTunnel: (changes are around line 112)
                         * Old code:
                         * if (l > 120)
                         * {
                         *     l = 120;
                         * }
                         *
                         * New code:
                         * // Increase max allowed generation height for nether caves to match the nether height
                         * if (l > this.world.getActualHeight() - 8)
                         * {
                         *     l = this.world.getActualHeight() - 8;
                         * }
                         */
                        if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 120) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/MapGenBase", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "world" : "field_75039_c", "Lnet/minecraft/world/World;"));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 8));
                            method.instructions.insertBefore(insn, new InsnNode(ISUB));
                            method.instructions.remove(insn);
                        }
                        /*
                         * addTunnel: (changes are around line 135)
                         * Old code:
                         * if (l1 >= 0 && l1 < 128)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Use actual height instead of hardcoded value
                         * if (l1 >= 0 && l1 < this.world.getActualHeight())
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/MapGenBase", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "world" : "field_75039_c", "Lnet/minecraft/world/World;"));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.remove(insn);
                        }
                        /*
                         * addTunnel: (changes are around line 139)
                         * Old code:
                         * if (iblockstate.getBlock() == Blocks.FLOWING_LAVA || iblockstate.getBlock() == Blocks.LAVA)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Avoid carving through any fluid block, not only just lava
                         * if (iblockstate.getMaterial().isLiquid() || iblockstate.getBlock() instanceof IFluidBlock)
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == GETSTATIC) {
                            // iblockstate.getMaterial().isLiquid()
                            if(((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "FLOWING_LAVA" : "field_150356_k")) {
                                ((JumpInsnNode)insn.getNext()).setOpcode(IFNE);
                                method.instructions.remove(insn.getPrevious());
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getMaterial" : "func_185904_a", "()Lnet/minecraft/block/material/Material;", true));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/material/Material", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "isLiquid" : "func_76224_d", "()Z", false));
                                method.instructions.remove(insn);
                            }
                            // iblockstate.getBlock() instanceof IFluidBlock
                            else if(((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "LAVA" : "field_150353_l")) {
                                ((JumpInsnNode)insn.getNext()).setOpcode(IFEQ);
                                method.instructions.insert(insn, new TypeInsnNode(INSTANCEOF, "net/minecraftforge/fluids/IFluidBlock"));
                                method.instructions.remove(insn);
                            }
                        }
                        /*
                         * addTunnel: (changes are around line 161)
                         * Old code:
                         * double d8 = ((double)(j3 + p_180704_4_ * 16) + 0.5D - p_180704_10_) / d2;
                         *
                         * New code:
                         * //cache biome so its blocks can be accounted for
                         * double d8 = ((double)(j3 + p_180704_4_ * 16) + 0.5D - p_180704_10_) / d2;
                         * Biome biome = Hooks.getBiome(this.world, p_180704_3_, p_180704_4_, i3, j3);
                         */
                        else if(insn.getOpcode() == DSTORE && ((VarInsnNode)insn).var == (FMLLaunchHandler.isDeobfuscatedEnvironment() ? 57 : 56)) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/MapGenBase", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "world" : "field_75039_c", "Lnet/minecraft/world/World;"));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new VarInsnNode(ILOAD, 50));
                            list.add(new VarInsnNode(ILOAD, 53));
                            list.add(new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerMapGenCavesHell$Hooks", "getBiome", "(Lnet/minecraft/world/World;IIII)Lnet/minecraft/world/biome/Biome;", false));
                            list.add(new VarInsnNode(ASTORE, 62));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * addTunnel: (changes are around line 171)
                         * Old code:
                         * if (iblockstate1.getBlock() == Blocks.NETHERRACK || iblockstate1.getBlock() == Blocks.DIRT || iblockstate1.getBlock() == Blocks.GRASS)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * if (Hooks.canCarveThrough(iblockstate1, p_180704_5_, i3, i2, j3, biome) == true || iblockstate1.getBlock() == Blocks.DIRT || iblockstate1.getBlock() == Blocks.GRASS)
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == ALOAD && ((VarInsnNode)insn).var == (FMLLaunchHandler.isDeobfuscatedEnvironment() ? 59 : 61)) {
                            method.instructions.remove(insn.getNext());
                            method.instructions.remove(insn.getNext());

                            ((JumpInsnNode)insn.getNext()).setOpcode(IF_ICMPEQ);
                            method.instructions.insert(insn, new InsnNode(ICONST_1));
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerMapGenCavesHell$Hooks", "canCarveThrough", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/chunk/ChunkPrimer;IIILnet/minecraft/world/biome/Biome;)Z", false));
                            method.instructions.insert(insn, new VarInsnNode(ALOAD, 62));
                            method.instructions.insert(insn, new VarInsnNode(ILOAD, 53));
                            method.instructions.insert(insn, new VarInsnNode(ILOAD, FMLLaunchHandler.isDeobfuscatedEnvironment() ? 56 : 58));
                            method.instructions.insert(insn, new VarInsnNode(ILOAD, 50));
                            method.instructions.insert(insn, new VarInsnNode(ALOAD, 5));
                            break;
                        }
                    }
                }

                // recursiveGenerate
                else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "recursiveGenerate" : "")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * recursiveGenerate: (changes are around line 195)
                         * Old code:
                         * int i = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(10) + 1) + 1);
                         *
                         * New code:
                         * // Generate twice as many caves if the nether height is twice as big
                         * int i = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(10) + 1) + 1) << (worldIn.getActualHeight() >> 8);
                         */
                        if(insn.getOpcode() == ISTORE && ((VarInsnNode)insn).var == 7 && insn.getPrevious().getOpcode() == INVOKEVIRTUAL) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 8));
                            method.instructions.insertBefore(insn, new InsnNode(ISHR));
                            method.instructions.insertBefore(insn, new InsnNode(ISHL));
                        }
                        /*
                         * recursiveGenerate: (changes are around line 195)
                         * Old code:
                         * if (this.rand.nextInt(5) != 0)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Generate twice as many caves if the nether height is twice as big
                         * if (this.rand.nextInt(5 >> (worldIn.getActualHeight() >> 8)) != 0)
                         * {
                         *     ...
                         * }
                         */
                        else if(insn.getOpcode() == ICONST_5) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            list.add(new IntInsnNode(BIPUSH, 8));
                            list.add(new InsnNode(ISHR));
                            list.add(new InsnNode(ISHR));
                            method.instructions.insert(insn, list);
                        }
                        /*
                         * recursiveGenerate: (changes are around line 205)
                         * Old code:
                         * double d1 = (double)this.rand.nextInt(128);
                         *
                         * New code:
                         * // Use actual height instead of hardcoded value
                         * double d1 = (double)this.rand.nextInt(worldIn.getActualHeight());
                         */
                        else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                            method.instructions.remove(insn);
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Biome getBiome(@Nonnull World world, int chunkX, int chunkZ, int x, int z) {
            return world.getBiome(new BlockPos((chunkX << 4) + x, 0, (chunkZ << 4) + z));
        }

        public static boolean canCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z, @Nonnull Biome biome) {
            if(state.getBlock() instanceof INetherCarvable) return ((INetherCarvable)state.getBlock()).canNetherCarveThrough(state, primer, x, y, z);
            return state.getBlock() == Blocks.NETHERRACK || state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.END_STONE //built-in
                    || (biome instanceof INetherCarvable ? ((INetherCarvable)biome).canNetherCarveThrough(state, primer, x, y, z) : biome.topBlock == state || biome.fillerBlock == state);
        }
    }
}
