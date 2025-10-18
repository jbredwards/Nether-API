/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Allow any dimension to permit respawns
 * @author jbred
 *
 */
public final class TransformerPlayerChunkMap implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            case "net.minecraft.server.management.PlayerChunkMap":
            case "net.minecraft.world.WorldServer": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "tick" : "func_72835_b") || method.name.equals(DEOBFUSCATED ? "createSpawnPosition" : "func_73052_b"), (method, insn) -> {
                    /*
                     * Old code:
                     * if (!worldprovider.canRespawnHere())
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Check override and config settings.
                     * if (!git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic.canSpawnInDimension(worldprovider, null))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "canRespawnHere" : "func_76567_e")) {
                        method.instructions.insertBefore(insn, new InsnNode(ACONST_NULL));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/common/world/PlayerSpawnLogic", "canSpawnInDimension", "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            case "net.minecraft.server.management.PlayerList": {
                @Nonnull final IntegerHolder index = new IntegerHolder();
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "recreatePlayerEntity" : "func_72368_a"), (method, insn) -> {
                    /*
                     * Old code:
                     * else if (!this.world.provider.canRespawnHere())
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Check override and config settings.
                     * else if (!git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic.canSpawnInDimension(this.world.provider, playerIn))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "canRespawnHere" : "func_76567_e")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/common/world/PlayerSpawnLogic", "canSpawnInDimension", "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
                        method.instructions.remove(insn);
                    }
                    /*
                     * Old code:
                     * if (server.getWorld(dimension) == null) dimension = 0;
                     *
                     * New code:
                     * // Always respect player-set spawn dimensions.
                     * if (server.getWorld(dimension = Hooks.getFallbackDimension(playerIn, dimension)) == null) dimension = 0;
                     */
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getWorld" : "func_71218_a")) {
                        method.instructions.insert(insn, new VarInsnNode(ISTORE, 2));
                        method.instructions.insert(insn, genHookMethod("getFallbackDimension", "(Lnet/minecraft/entity/player/EntityPlayer;I)I"));
                        method.instructions.insert(insn, new VarInsnNode(ILOAD, 2));
                        method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
                        if(++index.value == 2) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            case "net.minecraft.world.gen.ChunkProviderServer": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "queueUnload" : "func_189549_a"), (method, insn) -> {
                    /*
                     * Old code:
                     * if (this.world.provider.canDropChunk(chunkIn.x, chunkIn.z))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Check override and config settings for spawn chunks.
                     * if (Hooks.canDropChunk(this.world.provider, chunkIn.x, chunkIn.z, this))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "canDropChunk" : "func_186056_c")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, genHookMethod("canDropChunk", "(Lnet/minecraft/world/WorldProvider;IILnet/minecraft/world/gen/ChunkProviderServer;)Z"));
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
        public static boolean canDropChunk(@Nonnull final WorldProvider provider, final int x, final int z, @Nonnull final ChunkProviderServer manager) {
            return provider.canDropChunk(x, z) && (!manager.world.isSpawnChunk(x, z) || !PlayerSpawnLogic.canSpawnInDimension(provider, null));
        }

        public static int getFallbackDimension(@Nonnull final EntityPlayer player, final int fallback) {
            return player.hasSpawnDimension() ? player.getSpawnDimension() : fallback;
        }
    }
}
