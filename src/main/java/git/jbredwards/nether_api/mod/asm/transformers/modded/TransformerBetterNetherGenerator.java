/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import git.jbredwards.nether_api.mod.common.compat.betternether.BiomeBetterNether;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import paulevs.betternether.biomes.BiomeRegister;
import paulevs.betternether.biomes.NetherBiome;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Allow BetterNether to use real biomes instead of pseudo-biomes
 * @author jbred
 *
 */
public final class TransformerBetterNetherGenerator implements IClassTransformer, Opcodes
{
    //exists in case BetterNether-Continuation adds built-in support
    public static boolean isEnabled = true;

    static void legacyTransformer(@Nonnull ClassNode classNode) {
        for(final MethodNode method : classNode.methods) {
            if(method.name.equals("generate")) {
                //needed down the line
                //get index dynamically, since there are multiple BetterNether forks that all influence this method
                int biomeIndex = -1;
                int wzIndex = -1;
                for(final LocalVariableNode var : method.localVariables) {
                    if(var.name.equals("wz")) wzIndex = var.index;
                    else if(var.name.equals("biome")) biomeIndex = var.index;
                }

                //generate
                for(final AbstractInsnNode insn : method.instructions.toArray()) {
                    /*
                     * generate:
                     * Old code:
                     * makeBiomeArray(world, sx, sz);
                     *
                     * New code:
                     * //remove unneeded biome array creation (it isn't used while this mod is installed)
                     * -----------------------------;
                     */
                    if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("makeBiomeArray")) {
                        if(((MethodInsnNode)insn).desc.startsWith("(L")) method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn);
                    }
                    /*
                     * generate:
                     * Old code:
                     * int wz = sz + z;
                     *
                     * New code:
                     * //set local biome variable early, to dramatically increase performance
                     * int wz = sz + z;
                     * biome = Hooks.getNetherBiome(world, wx, wz);
                     */
                    else if(insn.getOpcode() == ISTORE && ((VarInsnNode)insn).var == wzIndex) {
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new VarInsnNode(ILOAD, wzIndex - 2));
                        list.add(new VarInsnNode(ILOAD, wzIndex));
                        list.add(new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/TransformerBetterNetherGenerator$Hooks", "getNetherBiome", "(Lnet/minecraft/world/World;II)Lpaulevs/betternether/biomes/NetherBiome;", false));
                        list.add(new VarInsnNode(ASTORE, biomeIndex));
                        method.instructions.insert(insn, list);
                    }
                    /*
                     * generate:
                     * Old code:
                     * biome = getBiomeLocal(x, y, z,world, random);
                     *
                     * New code:
                     * //remove unneeded local biome variable assignment, handled via above transformer
                     * --------------------------------------------;
                     */
                    else if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("getBiomeLocal")) {
                        if(((MethodInsnNode)insn).desc.contains("World")) method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getNext());
                        method.instructions.remove(insn);
                        return;
                    }
                }
            }
        }
    };

    static void continuationTransformer(@Nonnull ClassNode classNode) {
        for(final MethodNode method : classNode.methods) {
            if(method.name.equals("generate")) {
                //generate
                for(final AbstractInsnNode insn : method.instructions.toArray()) {
                    /*
                     * generate:
                     * Old code:
                     * makeBiomeArray(world, sx, sz);
                     *
                     * New code:
                     * //remove unneeded biome array creation (it isn't used while this mod is installed)
                     * -----------------------------;
                     */
                    if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("makeBiomeArray")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn);
                    }
                    /*
                     * generate:
                     * Old code:
                     * biome.set(getBiomeFromCache(pos.getX() & 15, pos.getZ() & 15, biomeCache, world, r));
                     *
                     * New code:
                     * //use real biome instances instead of generating pseudo-biomes
                     * biome.set(Hooks.getNetherBiome(pos.getX() & 15, pos.getZ() & 15, biomeCache, world, r, pos));
                     */
                    else if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("getBiomeFromCache")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 10));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/TransformerBetterNetherGenerator$Hooks", "getNetherBiome", "(II[[Lpaulevs/betternether/biomes/NetherBiome;Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Lpaulevs/betternether/biomes/NetherBiome;", false));
                        method.instructions.remove(insn);
                        return;
                    }
                }
            }
        }
    };

    @Nonnull
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(isEnabled && transformedName.equals("paulevs.betternether.world.BNWorldGenerator")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);

            //search for method existing in only legacy versions
            boolean useLegacyTransformer = false;
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals("smoothChunk")) {
                    useLegacyTransformer = true;
                    break;
                }
            }

            if(useLegacyTransformer) legacyTransformer(classNode);
            else continuationTransformer(classNode);

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
        public static NetherBiome getNetherBiome(@Nonnull World world, int wx, int wz) {
            final Biome biome = world.getBiome(new BlockPos(wx, 0, wz));
            return biome instanceof BiomeBetterNether ? ((BiomeBetterNether)biome).netherBiome : BiomeRegister.BIOME_EMPTY_NETHER;
        }

        @Nonnull
        public static NetherBiome getNetherBiome(int x, int z, @Nonnull NetherBiome[][] cache, @Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos) {
            return cache[x][z] == null ? (cache[x][z] = getNetherBiome(world, pos.getX(), pos.getZ())) : cache[x][z];
        }
    }
}
