/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.network.MessageTeleportFX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * <a href="https://bugs.mojang.com/browse/MC-10369">MC-10369</a> - Server side particle spawning does not create particles for client.
 * <p> Note that <a href="https://bugs.mojang.com/browse/MC-2518">MC-2518</a> is fixed by Forge.
 * @author jbred
 *
 */
public final class Transformer_MC_10369 implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        switch(transformedName) {
            // BlockLiquid
            case "net.minecraft.block.BlockLiquid": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "triggerMixEffects" : "func_180688_d")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnFluidParticles", "(Lnet/minecraft/world/World;DDD)I", false));
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

            // BlockPumpkin
            case "net.minecraft.block.BlockPumpkin": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "trySpawnGolem" : "func_180673_e")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_180501_a")) {
                                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "destroyBlockWithFlags", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", false));
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
                            else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 128) ((IntInsnNode)insn).operand = 0;
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // BlockRedstoneTorch
            case "net.minecraft.block.BlockRedstoneTorch": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "updateTick" : "func_180650_b")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnTorchParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)I", false));
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

            // BlockSkull
            case "net.minecraft.block.BlockSkull": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "checkWitherSpawn" : "func_180679_a")) {
                        int index = 0;
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            /*
                             * checkWitherSpawn: (changes are around line 245)
                             * Old code:
                             * worldIn.setBlockState(blockworldstate1.getPos(), Blocks.AIR.getDefaultState(), 2);
                             *
                             * New code:
                             * // Add block destroy effects when spawning a wither
                             * Hooks.destroyBlockWithFlags(worldIn, blockworldstate1.getPos(), Blocks.AIR.getDefaultState(), 2);
                             */
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_180501_a") && ++index == 2) {
                                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "destroyBlockWithFlags", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", false));
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

            // EntityAIMate
            case "net.minecraft.entity.ai.EntityAIMate": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spawnBaby" : "func_75388_i")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAIMate", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "world" : "field_75394_a", "Lnet/minecraft/world/World;"));
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                                method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAIMate", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "animal" : "field_75390_d", "Lnet/minecraft/entity/passive/EntityAnimal;"));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnBabyParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)I", false));
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

            // EntityDragon
            case "net.minecraft.entity.boss.EntityDragon": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "destroyBlocksInAABB" : "func_70972_a")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            /*
                             * destroyBlocksInAABB: (changes are around line 577)
                             * Old code:
                             * this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                             *
                             * New code:
                             * // Fix dragon block breaking particle
                             * Hooks.spawnDragonParticle(this.world, EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                             */
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spawnParticle" : "func_175688_a")) {
                                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnDragonParticle", "(Lnet/minecraft/world/World;Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V", false));
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

            // EntityLivingBase
            case "net.minecraft.entity.EntityLivingBase": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "attemptTeleport" : "func_184595_k")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnTeleportParticles", "(Lnet/minecraft/entity/Entity;DDD)I", false));
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

            // ItemEnderEye
            case "net.minecraft.item.ItemEnderEye": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);

                methods:
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onItemUse" : "func_180614_a")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/Transformer_MC_10369$Hooks", "spawnEyeParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", false));
                                method.instructions.remove(insn);
                                break methods;
                            }
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // RandomPatches mod compat:
            // Disable RandomPatches' particle fix (since it can cause bugs with other mods in general, like particles being spawned twice)
            case "com.therandomlabs.randompatches.patch.ServerWorldEventHandlerPatch": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                for(final MethodNode method : classNode.methods) {
                    if(method.name.equals("apply")) {
                        method.instructions.clear();
                        method.instructions.add(new InsnNode(ICONST_1));
                        method.instructions.add(new InsnNode(IRETURN));
                        break;
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
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
