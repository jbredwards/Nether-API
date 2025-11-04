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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Use seed-coord-based random and un-hardcode obsidian spike generation
 * @author jbred
 *
 */
public final class TransformerBiomeEndDecorator implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transform(basicClass, classNode -> {
            // Remove final modifier from spikeGen field
            for(@Nonnull final FieldNode field : classNode.fields) {
                if(field.name.equals(DEOBFUSCATED ? "spikeGen" : "field_76835_L")) {
                    field.access &=~ ACC_FINAL;
                    break;
                }
            }

            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "genDecorations" : "func_150513_a"), (method, insn) -> {
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
                    method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/world/biome/BiomeEndDecorator", DEOBFUSCATED ? "spikeGen" : "field_76835_L", "Lnet/minecraft/world/gen/feature/WorldGenSpikes;"));
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
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        });
    }
}
