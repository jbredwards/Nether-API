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

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_BiomesOPlenty implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            // BOPHellBiome
            case "biomesoplenty.common.biome.nether.BOPHellBiome": {
                /*
                 * getBlockPos:
                 * Old code:
                 * int localY = 127;
                 * ...
                 * for(roofOffset = 2; roofOffset <= roofDepth && localY + roofOffset <= 127; ++roofOffset)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Use actual nether height instead of a hardcoded value
                 * int localY = world.getActualHeight() - 1;
                 * ...
                 * for(roofOffset = 2; roofOffset <= roofDepth && localY + roofOffset <= world.getActualHeight() - 1; ++roofOffset)
                 * {
                 *     ...
                 * }
                 */
                @Nonnull final IntegerHolder changes = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "genTerrainBlocks" : "func_180622_a"), (method, insn) -> {
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 127) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                        method.instructions.insertBefore(insn, new InsnNode(ISUB));
                        method.instructions.remove(insn);
                        if(++changes.value == 2) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // GeneratorUtils.ScatterYMethod
            case "biomesoplenty.common.util.biome.GeneratorUtils$ScatterYMethod": {
                /*
                 * getBlockPos:
                 * Old code:
                 * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, 122), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirAbove().create());
                 * ...
                 * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, 122), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirBelow().create());
                 *
                 * New code:
                 * // Use actual nether height instead of a hardcoded value
                 * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, world.getActualHeight() - 6), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirAbove().create());
                 * ...
                 * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, world.getActualHeight() - 6), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirBelow().create());
                 */
                @Nonnull final IntegerHolder changes = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals("getBlockPos"), (method, insn) -> {
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 122) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 6));
                        method.instructions.insertBefore(insn, new InsnNode(ISUB));
                        method.instructions.remove(insn);
                        if(++changes.value == 2) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // BlockQuery.BlockPosQueryAltitude
            case "biomesoplenty.common.util.block.BlockQuery$BlockPosQueryAltitude": {
                /*
                 * getBlockPos:
                 * Old code:
                 * return pos.getY() >= this.minHeight && pos.getY() <= this.maxHeight;
                 *
                 * New code:
                 * // Change max value based on nether height
                 * return pos.getY() >= this.minHeight && pos.getY() <= Hooks.maxHeight(this.maxHeight, world);
                 */
                return transformMethod(basicClass, method -> method.name.equals("matches"), (method, insn) -> {
                    if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("maxHeight")) {
                        method.instructions.insert(insn, genHookMethod("maxHeight", "(ILnet/minecraft/world/World;)I"));
                        method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
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
        public static int maxHeight(final int maxHeight, @Nonnull final World world) {
            return world.provider.isNether() ? maxHeight + (world.getActualHeight() >> 8 << 7) : maxHeight;
        }
    }
}
