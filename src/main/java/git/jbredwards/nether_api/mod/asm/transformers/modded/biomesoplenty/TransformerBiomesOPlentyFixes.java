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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Disable Biomes O' Plenty's fog override in dimensions with their own fog handlers
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyFixes implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            // FogEventHandler | PlanetEventHandler (Advanced Rocketry has this issue as well, reuse this transformer)
            case "biomesoplenty.common.handler.FogEventHandler":
            case "zmaster587.advancedRocketry.event.PlanetEventHandler":
                /*
                 * onGetFogColor & onRenderFog:
                 * Old code:
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Cancel event if the world provider already implements custom fog
                 * {
                 *     if (event.getEntity().world.provider instanceof git.jbredwards.nether_api.mod.common.world.IFogWorldProvider) return;
                 *     ...
                 * }
                 */
                return transformMethod(basicClass, method -> method.name.equals("onGetFogColor") || method.name.equals("onRenderFog") || method.name.equals("fogColor"), (method, insn) -> {
                    @Nonnull final InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/client/event/EntityViewRenderEvent", "getEntity", "()Lnet/minecraft/entity/Entity;", false));
                    list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/Entity", DEOBFUSCATED ? "world" : "field_70170_p", "Lnet/minecraft/world/World;"));
                    list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", DEOBFUSCATED ? "provider" : "field_73011_w", "Lnet/minecraft/world/WorldProvider;"));
                    list.add(new TypeInsnNode(INSTANCEOF, "git/jbredwards/nether_api/mod/common/world/IFogWorldProvider"));

                    @Nonnull final LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(IFEQ, label));
                    list.add(new InsnNode(RETURN));

                    list.add(label);
                    list.add(new FrameNode(F_SAME, 0, null, 0, null));
                    method.instructions.insert(method.instructions.getFirst(), list);

                    return BreakType.INSTRUCTIONS;
                });

            // GeneratorBramble
            case "biomesoplenty.common.world.generator.GeneratorBramble": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "generate" : "func_180709_b"), (method, insn) -> {
                    /*
                     * generate:
                     * Old code:
                     * world.setBlockState(genPos, this.with);
                     * ...
                     * world.setBlockState(genPos.offset(EnumFacing.values()[leafDirection]), Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false));
                     *
                     * New code:
                     * // Don't use bad block flags
                     * world.setBlockState(genPos, this.with, 18);
                     * ...
                     * world.setBlockState(genPos.offset(EnumFacing.values()[leafDirection]), Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false), 18);
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_175656_a")) {
                        method.instructions.insertBefore(insn, genBlockFlags());
                        if(!DEOBFUSCATED) ((MethodInsnNode)insn).name = "func_180501_a";
                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                    }
                    /*
                     * generate:
                     * Old code:
                     * if (world.isAirBlock(genPos.offset(EnumFacing.values()[leafDirection])))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * //
                     * if (Hooks.canPlaceLeaves(world, genPos.offset(EnumFacing.values()[leafDirection]), pos))
                     * {
                     *     ...
                     * }
                     */
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "isAirBlock" : "func_175623_d")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                        method.instructions.insertBefore(insn, genHookMethod("canPlaceLeaves", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * generate:
                     * Old code:
                     * genPos = genPos.offset(EnumFacing.values()[direction]);
                     *
                     * New code:
                     * // Don't cause cascading world gen
                     * genPos = Hooks.getClampedOffset(genPos, EnumFacing.values()[direction], pos);
                     */
                    else if(insn.getNext().getOpcode() == ASTORE && ((VarInsnNode)insn.getNext()).var == 5 && insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "offset" : "func_177972_a")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                        method.instructions.insertBefore(insn, genHookMethod("getClampedOffset", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // GeneratorHive
            case "biomesoplenty.common.world.generator.GeneratorHive": {
                return transform(basicClass, classNode -> {
                    /*
                     * Constructor:
                     * Old code:
                     * this.maxRadius = maxRadius;
                     *
                     * New code:
                     * // Cap max radius
                     * this.maxRadius = Math.min(maxRadius, 8);
                     */
                    transformMethod(classNode, method -> method.name.equals("<init>"), (method, insn) -> {
                        if(insn.getOpcode() == PUTFIELD && ((FieldInsnNode)insn).name.equals("maxRadius")) {
                            method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 8));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "min", "(II)I", false));
                            return BreakType.METHODS;
                        }

                        return BreakType.CONTINUE;
                    });
                    /*
                     * generate
                     * Old code:
                     * world.setBlockState(realPos, fillBlock, 2);
                     *
                     * New code:
                     * // Don't use bad block flags
                     * world.setBlockState(realPos, fillBlock, 18);
                     */
                    transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "generate" : "func_180709_b"), (method, insn) -> {
                        if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_180501_a")) {
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.insertBefore(insn, genBlockFlags());
                        }

                        return BreakType.CONTINUE;
                    });
                });
            }
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canPlaceLeaves(@Nonnull final World world, @Nonnull final BlockPos genPos, @Nonnull final BlockPos origin) {
            final int minX = (origin.getX() - 8) &~ 15;
            if(genPos.getX() < minX || genPos.getX() > minX + 31) return false;

            final int minZ = (origin.getZ() - 8) &~ 15;
            return genPos.getZ() >= minZ && genPos.getZ() <= minZ + 31 && world.isAirBlock(genPos);
        }

        @Nonnull
        public static BlockPos getClampedOffset(@Nonnull final BlockPos genPos, @Nonnull final EnumFacing offset, @Nonnull final BlockPos origin) {
            final int x = genPos.getX() + offset.getXOffset();
            final int minX = (origin.getX() - 8) &~ 15;
            if(x < minX || x > minX + 31) return genPos;

            final int z = genPos.getZ() + offset.getZOffset();
            final int minZ = (origin.getZ() - 8) &~ 15;
            return z < minZ || z > minZ + 31 ? genPos : genPos.offset(offset);
        }
    }
}
