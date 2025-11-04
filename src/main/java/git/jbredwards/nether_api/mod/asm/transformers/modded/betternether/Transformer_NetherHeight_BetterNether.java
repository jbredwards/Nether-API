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

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_BetterNether implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            // StructureEye
            case "paulevs.betternether.structures.plants.StructureEye": {
                @Nonnull final IntegerHolder count = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals("generate"), (method, insn) -> {
                    /*
                     * Old code:
                     * if (world.getBiome(pos) == Biomes.HELL && world.getBiome(pos.up()) == Biomes.HELL && world.getBiome(pos.up(2)) == Biomes.HELL)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Remove biome check.
                     * if (Hooks.hell(world, pos) == Biomes.HELL && Hooks.hell(world, pos.up()) == Biomes.HELL && Hooks.hell(world, pos.up(2)) == Biomes.HELL)
                     * {
                     *     ...
                     * }
                     */
                    if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getBiome" : "func_180494_b")) {
                        method.instructions.insert(insn, genHookMethod("hell", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"));
                        method.instructions.remove(insn);
                        if(++count.value == 3) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // StructureStalagnate
            case "paulevs.betternether.structures.plants.StructureStalagnate": {
                return transformMethod(basicClass, method -> method.name.equals("upRay"), (method, insn) -> {
                    /*
                     * Old code:
                     * for (int j = start.getY() + 1; j < 126; j++)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value.
                     * for (int j = start.getY() + 1; j < chunk.getActualHeight() - 2; j++)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 126) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.insertBefore(insn, new InsnNode(ICONST_2));
                        method.instructions.insertBefore(insn, new InsnNode(ISUB));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Biome hell(@Nonnull final World world, @Nonnull final BlockPos pos) { return Biomes.HELL; }
    }
}
