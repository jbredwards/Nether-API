/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Use seed-coord-based random and un-hardcode obsidian spike regeneration
 * @author jbred
 *
 */
public final class TransformerDragonSpawnManager implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("net.minecraft.world.end.DragonSpawnManager$3")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "process" : "func_186079_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == NEW) {
                            /*
                             * process: (changes are around line 76)
                             * Old code:
                             * WorldGenSpikes worldgenspikes = new WorldGenSpikes();
                             *
                             * New code:
                             * // Un-hardcode obsidian spike generator
                             * WorldGenSpikes worldgenspikes = WorldProviderTheEnd.END_PILLAR;
                             */
                            if(((TypeInsnNode)insn).desc.equals("net/minecraft/world/gen/feature/WorldGenSpikes")) {
                                method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/mod/common/world/WorldProviderTheEnd", "END_PILLAR", "Lnet/minecraft/world/gen/feature/WorldGenSpikes;"));
                                method.instructions.remove(insn.getNext());
                                method.instructions.remove(insn.getNext());
                                method.instructions.remove(insn);
                            }
                            /*
                             * process: (changes are around line 80)
                             * Old code:
                             * worldgenspikes.generate(worldIn, new Random(), new BlockPos(worldgenspikes$endspike.getCenterX(), 45, worldgenspikes$endspike.getCenterZ()));
                             *
                             * New code:
                             * // Use seed-based & coord-based random
                             * worldgenspikes.generate(worldIn, Hooks.createRandom(worldIn, worldgenspikes$endspike), new BlockPos(worldgenspikes$endspike.getCenterX(), 45, worldgenspikes$endspike.getCenterZ()));
                             */
                            else if(((TypeInsnNode)insn).desc.equals("java/util/Random")) {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 11));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerDragonSpawnManager$Hooks", "createRandom", "(Lnet/minecraft/world/World;Lnet/minecraft/world/gen/feature/WorldGenSpikes$EndSpike;)Ljava/util/Random;", false));
                                method.instructions.remove(insn.getNext());
                                method.instructions.remove(insn.getNext());
                                method.instructions.remove(insn);
                                break methods;
                            }
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

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Random createRandom(@Nonnull World world, @Nonnull WorldGenSpikes.EndSpike spike) {
            final Random rand = new Random(world.getSeed());
            rand.setSeed(((rand.nextLong() >> 2 + 1) * spike.getCenterX() + (rand.nextLong() >> 2 + 1) * spike.getCenterZ()) ^ world.getSeed());
            return rand;
        }
    }
}
