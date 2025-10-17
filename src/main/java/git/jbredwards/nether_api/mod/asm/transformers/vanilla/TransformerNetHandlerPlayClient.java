/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import com.mojang.authlib.GameProfile;
import git.jbredwards.nether_api.api.world.PlayerSpawnLogic;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Allow the Nether or End to be set as base spawn dimensions
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
        @Nonnull
        public static Function<GameProfile, Integer> PER_PLAYER = profile -> null;
        public static int getSpawnDim(final int fallback, @Nullable final GameProfile profile) {
            @Nonnull final WorldInfo info = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
            if(profile != null) {
                // Allow mods to define spawn dimensions per-player.
                // Note: Some modded dimensions may not work as expected, if they use hardcoded respawn logic themselves.
                @Nullable final Integer scriptDim = PER_PLAYER.apply(profile);
                if(scriptDim != null) return scriptDim;
            }

            // Read saved data from Nether, with a fallback on the override and then the config setting.
            @Nonnull final NBTTagCompound netherData = info.getDimensionData(DimensionType.NETHER.getId());
            if(netherData.getBoolean(NetherAPI.MODID + ":allowRespawn")) return DimensionType.NETHER.getId();
            else if(!netherData.hasKey(NetherAPI.MODID + ":allowRespawn", Constants.NBT.TAG_ANY_NUMERIC)) {
                if(WorldProviderNether.ALLOW_RESPAWN != null) { if(WorldProviderNether.ALLOW_RESPAWN) return DimensionType.NETHER.getId(); }
                else if(NetherAPIConfig.worldForSpawn == NetherAPIConfig.WorldForSpawn.NETHER) return DimensionType.NETHER.getId();
            }

            // Read saved data from The End, with a fallback on the override and then the config setting.
            @Nonnull final NBTTagCompound endData = info.getDimensionData(DimensionType.THE_END.getId());
            if(endData.getBoolean(NetherAPI.MODID + ":allowRespawn")) return DimensionType.THE_END.getId();
            else if(!endData.hasKey(NetherAPI.MODID + ":allowRespawn", Constants.NBT.TAG_ANY_NUMERIC)) {
                if(WorldProviderTheEnd.ALLOW_RESPAWN != null) { if(WorldProviderTheEnd.ALLOW_RESPAWN) return DimensionType.THE_END.getId(); }
                else if(NetherAPIConfig.worldForSpawn == NetherAPIConfig.WorldForSpawn.END) return DimensionType.THE_END.getId();
            }

            return fallback;
        }
    }
}
