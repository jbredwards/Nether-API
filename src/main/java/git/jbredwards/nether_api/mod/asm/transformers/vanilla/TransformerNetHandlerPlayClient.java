/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import com.mojang.authlib.GameProfile;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.util.text.translation.I18n;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allow more than just the Overworld to be set as an initial spawn dimension
 * @author jbred
 *
 */
public final class TransformerNetHandlerPlayClient implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            /*
             * Old code:
             * this.client.player.dimension = packetIn.getDimension();
             *
             * New code:
             * // Prevent a possible dimension ID desync, by ensuring Forge's "NetworkDispatcher.getOverrideDimension" is always applied.
             * this.client.player.dimension = this.world.provider.getDimension();
             */
            case "net.minecraft.client.network.NetHandlerPlayClient": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "handleJoinGame" : "func_147282_a"), (method, insn) -> {
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getDimension" : "func_149194_f")) {
                        method.instructions.insert(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/WorldProvider", "getDimension", "()I", false));
                        method.instructions.insert(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/World", DEOBFUSCATED ? "provider" : "field_73011_w", "Lnet/minecraft/world/WorldProvider;"));
                        method.instructions.insert(insn, new FieldInsnNode(GETFIELD, "net/minecraft/client/network/NetHandlerPlayClient", DEOBFUSCATED ? "world" : "field_147300_g", "Lnet/minecraft/client/multiplayer/WorldClient;"));
                        method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));

                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
            /*
             * Old code:
             * return spawnDimension != null ? spawnDimension : 0;
             *
             * New code:
             * // Default to spawn dimension override instead of overworld.
             * return spawnDimension != null ? spawnDimension : Hooks.getSpawnDim(0, this.getGameProfile());
             */
            case "net.minecraft.entity.player.EntityPlayer": {
                return transformMethod(basicClass, method -> method.name.equals("getSpawnDimension"), (method, insn) -> {
                    if(insn.getOpcode() == ICONST_0) {
                        method.instructions.insert(insn, genHookMethod("getSpawnDim", "(ILcom/mojang/authlib/GameProfile;)I"));
                        method.instructions.insert(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", DEOBFUSCATED ? "getGameProfile" : "func_146103_bH", "()Lcom/mojang/authlib/GameProfile;", false));
                        method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
            /*
             * Old code:
             * server.getWorld(0);
             *
             * New code:
             * // Un-hardcode initial spawn dimension.
             * server.getWorld(Hooks.getSpawnDim(0, profile));
             */
            case "net.minecraft.server.management.PlayerList":
            case "net.minecraft.server.MinecraftServer": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "createPlayerForUser" : "func_148545_a") || method.name.equals(DEOBFUSCATED ? "initialWorldChunkLoad" : "func_71222_d"), (method, insn) -> {
                    if(insn instanceof MethodInsnNode && (((MethodInsnNode)insn).name.equals("getWorld") || ((MethodInsnNode)insn).name.equals("func_71218_a"))) {
                        method.instructions.insertBefore(insn, "net.minecraft.server.management.PlayerList".equals(transformedName) ? new VarInsnNode(ALOAD, 1) : new InsnNode(ACONST_NULL));
                        method.instructions.insertBefore(insn, genHookMethod("getSpawnDim", "(ILcom/mojang/authlib/GameProfile;)I"));
                    }

                    else if(insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst.equals("Preparing start region for level 0")) {
                        method.instructions.insertBefore(insn, genHookMethod("getStartingRegionString", "()Ljava/lang/String;"));
                        method.instructions.remove(insn);
                    }

                    return BreakType.CONTINUE;
                });
            }
            /*
             * Old code:
             * server.getWorld(0);
             *
             * New code:
             * // Un-hardcode initial spawn dimension.
             * server.getWorld(Hooks.getSpawnDim(0, profile));
             */
            case "net.minecraftforge.fml.common.network.handshake.NetworkDispatcher": {
                @Nonnull final IntegerHolder index = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals("serverInitiateHandshake"), (method, insn) -> {
                    if(insn.getOpcode() == IRETURN && ++index.value == 2) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/network/handshake/NetworkDispatcher", "player", "Lnet/minecraft/entity/player/EntityPlayerMP;"));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", DEOBFUSCATED ? "getGameProfile" : "func_146103_bH", "()Lcom/mojang/authlib/GameProfile;", false));
                        method.instructions.insertBefore(insn, genHookMethod("getSpawnDim", "(ILcom/mojang/authlib/GameProfile;)I"));
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
        public static int getSpawnDim(final int fallback, @Nullable final GameProfile profile) {
            return PlayerSpawnLogic.getInitialSpawnDimension(profile);
        }

        @Nonnull
        public static String getStartingRegionString() {
            return I18n.translateToLocalFormatted("info." + NetherAPI.MODID + ".loadStartRegion", getSpawnDim(0, null));
        }
    }
}
