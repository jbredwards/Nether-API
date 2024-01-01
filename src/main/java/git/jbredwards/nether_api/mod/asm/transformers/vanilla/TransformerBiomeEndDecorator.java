/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Use seed-coord-based random and un-hardcode obsidian spike generation
 * @author jbred
 *
 */
public final class TransformerBiomeEndDecorator implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("net.minecraft.world.biome.BiomeEndDecorator")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            //remove final modifier from spikeGen field
            for(final FieldNode field : classNode.fields) {
                if(field.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spikeGen" : "field_76835_L")) {
                    field.access &=~ ACC_FINAL;
                    break;
                }
            }

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "genDecorations" : "func_150513_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * genDecorations: (changes are around line 25)
                         * Old code:
                         * protected void genDecorations(Biome biomeIn, World worldIn, Random random)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Un-hardcode obsidian spike generator
                         * protected void genDecorations(Biome biomeIn, World worldIn, Random random)
                         * {
                         *     this.spikeGen = WorldProviderTheEnd.END_PILLAR;
                         *     ...
                         * }
                         */
                        if(insn.getPrevious() == method.instructions.getFirst()) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/mod/common/world/WorldProviderTheEnd", "END_PILLAR", "Lnet/minecraft/world/gen/feature/WorldGenSpikes;"));
                            method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/world/biome/BiomeEndDecorator", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spikeGen" : "field_76835_L", "Lnet/minecraft/world/gen/feature/WorldGenSpikes;"));
                        }
                        /*
                         * genDecorations: (changes are around line 25)
                         * Old code:
                         * this.spikeGen.generate(worldIn, random, new BlockPos(worldgenspikes$endspike.getCenterX(), 45, worldgenspikes$endspike.getCenterZ()));
                         *
                         * New code:
                         * // Use seed-based & coord-based random
                         * this.spikeGen.generate(worldIn, Hooks.createRandom(worldIn, worldgenspikes$endspike), new BlockPos(worldgenspikes$endspike.getCenterX(), 45, worldgenspikes$endspike.getCenterZ()));
                         */
                        else if(insn.getOpcode() == ALOAD && ((VarInsnNode)insn).var == 3 && insn.getNext().getOpcode() != INVOKEVIRTUAL) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 8));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerDragonSpawnManager$Hooks", "createRandom", "(Lnet/minecraft/world/World;Lnet/minecraft/world/gen/feature/WorldGenSpikes$EndSpike;)Ljava/util/Random;", false));
                            method.instructions.remove(insn);
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
