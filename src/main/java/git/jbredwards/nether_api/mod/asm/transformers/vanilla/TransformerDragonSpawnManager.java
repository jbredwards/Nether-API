/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Use seed-coord-based random and un-hardcode obsidian spike regeneration
 * @author jbred
 *
 */
public final class TransformerDragonSpawnManager implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "process" : "func_186079_a"), (method, insn) -> {
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
                    method.instructions.insertBefore(insn, genHookMethod("createRandom", "(Lnet/minecraft/world/World;Lnet/minecraft/world/gen/feature/WorldGenSpikes$EndSpike;)Ljava/util/Random;"));
                    method.instructions.remove(insn.getNext());
                    method.instructions.remove(insn.getNext());
                    method.instructions.remove(insn);
                    return BreakType.METHODS;
                }
            }

            return BreakType.CONTINUE;
        });
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
