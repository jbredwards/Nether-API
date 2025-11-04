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

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import biomesoplenty.common.block.BlockBOPGrass;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.journey.init.blocks.JourneyBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.objectweb.asm.tree.*;
import paulevs.betternether.blocks.BlocksRegister;

import javax.annotation.Nonnull;

/**
 * Support the conversion of certain modded netherrack blocks
 * @author jbred
 *
 */
public final class TransformerInputHandler implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("onPlayerRightClick"), (method, insn) -> {
            /*
             * Old code:
             * if (stack.getItem() instanceof ItemSpade)
             * {
             *     ...
             * }
             *
             * New code:
             * // Account for all modded shovels.
             * if (stack.getItem().getToolClasses(stack).contains("shovel"))
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == INSTANCEOF) {
                ((JumpInsnNode)insn.getNext()).setOpcode(IFEQ);
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/item/Item", "getToolClasses", "(Lnet/minecraft/item/ItemStack;)Ljava/util/Set;", false));
                method.instructions.insertBefore(insn, new LdcInsnNode("shovel"));
                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true));
                method.instructions.remove(insn);
            }
            /*
             * Old code:
             * if (block == Blocks.NETHERRACK)
             * {
             *     ...
             * }
             *
             * New code:
             * // Support the conversion of certain modded netherrack blocks.
             * if (block == Hooks.getNetherrackCandidate(state, Blocks.NETHERRACK))
             * {
             *     ...
             * }
             */
            else if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "NETHERRACK" : "field_150424_aL")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 6));
                method.instructions.insert(insn, genHookMethod("getNetherrackCandidate", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Block getNetherrackCandidate(@Nonnull final IBlockState state, @Nonnull final Block fallback) {
            if(state.getBlock() != fallback) {
                if(NetherAPI.isBetterNetherLoaded) {
                    if(state.getBlock() == BlocksRegister.BLOCK_NETHER_MYCELIUM || state.getBlock() == BlocksRegister.BLOCK_NETHERRACK_MOSS) return state.getBlock();
                }
                if(NetherAPI.isBiomesOPlentyLoaded) {
                    if(state.getBlock() instanceof BlockBOPGrass) {
                        @Nonnull final BlockBOPGrass.BOPGrassType type = (BlockBOPGrass.BOPGrassType)state.getValue(BlockBOPGrass.VARIANT);
                        if(type == BlockBOPGrass.BOPGrassType.MYCELIAL_NETHERRACK || type == BlockBOPGrass.BOPGrassType.OVERGROWN_NETHERRACK) return state.getBlock();
                    }
                }
                if(NetherAPI.isJourneyIntoTheLightLoaded) {
                    if(state.getBlock() == JourneyBlocks.earthenNetherrack || state.getBlock() == JourneyBlocks.nethicGrass) return state.getBlock();
                }
            }

            return fallback;
        }
    }
}
