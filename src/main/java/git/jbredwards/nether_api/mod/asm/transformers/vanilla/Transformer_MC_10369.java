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

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.network.MessageTeleportFX;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * <a href="https://bugs-legacy.mojang.com/browse/MC-10369">MC-10369</a> - Server side particle spawning does not create particles for client.
 * <p> Note that <a href="https://bugs-legacy.mojang.com/browse/MC-2518">MC-2518</a> & <a href="https://bugs-legacy.mojang.com/browse/MC-96974">MC-96974</a> are fixed by Forge.</p>
 * @author jbred
 *
 */
public final class Transformer_MC_10369 implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        switch(transformedName) {
            // BlockLiquid
            case "net.minecraft.block.BlockLiquid": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "triggerMixEffects" : "func_180688_d"), (method, insn) -> {
                    /*
                     * updateTick: (changes are around line 451)
                     * Old code:
                     * for (int i = 0; i < 8; ++i)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Disable old broken particle logic
                     * for (int i = 0; i < Hooks.spawnFluidParticles(worldIn, d0, d1, d2); ++i)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 8) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 3));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 5));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 7));
                        method.instructions.insertBefore(insn, genHookMethod("spawnFluidParticles", "(Lnet/minecraft/world/World;DDD)I"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // BlockPumpkin
            case "net.minecraft.block.BlockPumpkin": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "trySpawnGolem" : "func_180673_e"), (method, insn) -> {
                    /*
                     * trySpawnGolem: (changes are around line 74 & 108)
                     * Old code:
                     * worldIn.setBlockState(blockworldstate.getPos(), Blocks.AIR.getDefaultState(), 2);
                     * ...
                     * Hooks.destroyBlockWithFlags(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(), Blocks.AIR.getDefaultState(), 2);
                     *
                     * New code:
                     * // Add block destroy effects when spawning a golem
                     * worldIn.setBlockState(blockworldstate.getPos(), Blocks.AIR.getDefaultState(), 2);
                     * ...
                     * Hooks.destroyBlockWithFlags(blockpattern$patternhelper.translateOffset(j, k, 0).getPos(), Blocks.AIR.getDefaultState(), 2);
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_180501_a")) {
                        method.instructions.insert(insn, genHookMethod("destroyBlockWithFlags", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * trySpawnGolem: (changes are around line 87 & 123)
                     * Old code:
                     * for (int j1 = 0; j1 < 120; ++j1)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Disable old broken particle logic
                     * for (int j1 = 0; j1 < 0; ++j1)
                     * {
                     *     ...
                     * }
                     */
                    else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) ((IntInsnNode)insn).operand = 0;
                    return BreakType.CONTINUE;
                });
            }

            // BlockRedstoneTorch
            case "net.minecraft.block.BlockRedstoneTorch": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "updateTick" : "func_180650_b"), (method, insn) -> {
                    /*
                     * updateTick: (changes are around line 148)
                     * Old code:
                     * for (int i = 0; i < 5; ++i)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Disable old broken particle logic
                     * for (int i = 0; i < Hooks.spawnTorchParticles(worldIn, pos, state); ++i)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == ICONST_5) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                        method.instructions.insertBefore(insn, genHookMethod("spawnTorchParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)I"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // BlockSkull
            case "net.minecraft.block.BlockSkull": {
                @Nonnull final IntegerHolder index = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "checkWitherSpawn" : "func_180679_a"), (method, insn) -> {
                    /*
                     * checkWitherSpawn: (changes are around line 245)
                     * Old code:
                     * worldIn.setBlockState(blockworldstate1.getPos(), Blocks.AIR.getDefaultState(), 2);
                     *
                     * New code:
                     * // Add block destroy effects when spawning a wither
                     * Hooks.destroyBlockWithFlags(worldIn, blockworldstate1.getPos(), Blocks.AIR.getDefaultState(), 2);
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_180501_a") && ++index.value == 2) {
                        method.instructions.insert(insn, genHookMethod("destroyBlockWithFlags", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * checkWitherSpawn: (changes are around line 263)
                     * Old code:
                     * for (int l = 0; l < 120; ++l)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Disable old broken particle logic
                     * for (int l = 0; l < 0; ++l)
                     * {
                     *     ...
                     * }
                     */
                    else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 128) {
                        ((IntInsnNode)insn).operand = 0;
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // EntityAIMate
            case "net.minecraft.entity.ai.EntityAIMate": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "spawnBaby" : "func_75388_i"), (method, insn) -> {
                    /*
                     * spawnBaby: (changes are around line 152)
                     * Old code:
                     * for (int i = 0; i < 7; ++i)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Fix baby spawning particles
                     * for (int i = 0; i < Hooks.spawnBabyParticles(this.world, this.animal); ++i)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 7) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAIMate", DEOBFUSCATED ? "world" : "field_75394_a", "Lnet/minecraft/world/World;"));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAIMate", DEOBFUSCATED ? "animal" : "field_75390_d", "Lnet/minecraft/entity/passive/EntityAnimal;"));
                        method.instructions.insertBefore(insn, genHookMethod("spawnBabyParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)I"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // EntityDragon
            case "net.minecraft.entity.boss.EntityDragon": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "destroyBlocksInAABB" : "func_70972_a"), (method, insn) -> {
                    /*
                     * destroyBlocksInAABB: (changes are around line 577)
                     * Old code:
                     * this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                     *
                     * New code:
                     * // Fix dragon block breaking particle
                     * Hooks.spawnDragonParticle(this.world, EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "spawnParticle" : "func_175688_a")) {
                        method.instructions.insert(insn, genHookMethod("spawnDragonParticle", "(Lnet/minecraft/world/World;Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // EntityLivingBase
            case "net.minecraft.entity.EntityLivingBase": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "attemptTeleport" : "func_184595_k"), (method, insn) -> {
                    /*
                     * attemptTeleport: (changes are around line 577)
                     * Old code:
                     * for(int j = 0; j < 128; ++j)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Fix entity teleport particles
                     * for(int j = 0; j < Hooks.spawnTeleportParticle(this, d0, d1, d2); ++j)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == SIPUSH && insn.getNext().getOpcode() != ISTORE && ((IntInsnNode)insn).operand == 128) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 7));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 9));
                        method.instructions.insertBefore(insn, new VarInsnNode(DLOAD, 11));
                        method.instructions.insertBefore(insn, genHookMethod("spawnTeleportParticles", "(Lnet/minecraft/entity/Entity;DDD)I"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // ItemEnderEye
            case "net.minecraft.item.ItemEnderEye": {
                return transformMethod(basicClass, true, method -> method.name.equals(DEOBFUSCATED ? "onItemUse" : "func_180614_a"), (method, insn) -> {
                    /*
                     * onItemUse: (changes are around line 52)
                     * Old code:
                     * for (int i = 0; i < 16; ++i)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Fix eye of ender placement particle
                     * for (int i = 0; i < Hooks.spawnEyeParticles(worldIn, pos); ++i)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 16) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                        method.instructions.insertBefore(insn, genHookMethod("spawnEyeParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I"));
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
        // mutable in case someone wants to disable any of these fixes
        public static boolean BABY_FIX = true, DRAGON_FIX = true, EYE_FIX = true, FLUID_FIX = true, GOLEM_FIX = true, TELEPORT_FIX = true, TORCH_FIX = true;

        public static boolean destroyBlockWithFlags(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState replaceState, int flags) {
            if(GOLEM_FIX) world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(world.getBlockState(pos)));
            return world.setBlockState(pos, replaceState, flags);
        }

        public static int spawnBabyParticles(@Nonnull World world, @Nonnull Entity animal) {
            if(BABY_FIX) world.setEntityState(animal, (byte)18);
            return 0;
        }

        public static void spawnDragonParticle(@Nonnull World world, @Nonnull EnumParticleTypes particle, double x, double y, double z, double motionX, double motionY, double motionZ, int[] args) {
            if(DRAGON_FIX && world instanceof WorldServer) ((WorldServer)world).spawnParticle(particle, true, x, y, z, 0, motionX, motionY, motionZ, 1, args);
        }

        public static int spawnEyeParticles(@Nonnull World world, @Nonnull BlockPos pos) {
            if(EYE_FIX && world instanceof WorldServer) ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5, pos.getY() + 0.8125, pos.getZ() + 0.5, 16, 0.1875, 0, 0.1875, 0);
            return 0; // 16
        }

        public static int spawnFluidParticles(@Nonnull World world, double x, double y, double z) {
            if(FLUID_FIX && world instanceof WorldServer) ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + 0.5, y + 1.2, z + 0.5, 8, 0.25, 0, 0.25, 0);
            return 0; // 8
        }

        public static int spawnTeleportParticles(@Nonnull Entity entity, double prevX, double prevY, double prevZ) {
            if(TELEPORT_FIX && entity.world instanceof WorldServer) {
                final MessageTeleportFX message = new MessageTeleportFX(entity, prevX, prevY, prevZ);
                if(entity instanceof EntityPlayerMP) NetherAPI.WRAPPER.sendTo(message, (EntityPlayerMP)entity);

                NetherAPI.WRAPPER.sendToAllTracking(message, entity);
            }

            return 0; // 128
        }

        public static int spawnTorchParticles(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
            if(TORCH_FIX && world instanceof WorldServer) {
                final EnumFacing facing = state.getValue(BlockTorch.FACING);
                double offsetX = 0.5, offsetY = 0.5625, offsetZ = 0.5;
                if(facing.getAxis().isHorizontal()) {
                    offsetX -= 0.27 * facing.getXOffset();
                    offsetY += 0.22;
                    offsetZ -= 0.27 * facing.getZOffset();
                }

                ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 5, 0.1, 0.1, 0.1, 0);
            }

            return 0; // 5
        }
    }
}
