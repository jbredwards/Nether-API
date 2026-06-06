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
import git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic;
import io.netty.util.internal.IntegerHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.IntPredicate;

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
                        if(transformedName.endsWith("PlayerChunkMap")) return BreakType.METHODS;
                    }
                    /*
                     * Old code:
                     * if (settings.isBonusChestEnabled())
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Only create bonus chest in the Overworld.
                     * if (Hooks.isBonusChestEnabled(settings, this))
                     * {
                     *     ...
                     * }
                     */
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "isBonusChestEnabled" : "func_77167_c")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, genHookMethod("isBonusChestEnabled", "(Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/World;)Z"));
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
                     * else if (!world.provider.canRespawnHere())
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Check override and config settings.
                     * else if (!git.jbredwards.nether_api.mod.common.world.PlayerSpawnLogic.canSpawnInDimension(world.provider, playerIn))
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
                     * if (server.getWorld(dimension = Hooks.getRespawnDimension(playerIn, world, dimension, conqueredEnd)) == null) dimension = 0;
                     */
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getWorld" : "func_71218_a") && ++index.value == 2) {
                        method.instructions.insertBefore(insn, new InsnNode(POP));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                        method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 3));
                        method.instructions.insertBefore(insn, genHookMethod("getRespawnDimension", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/world/World;IZ)I"));
                        method.instructions.insertBefore(insn, new VarInsnNode(ISTORE, 2));
                        method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 2));
                        return BreakType.METHODS;
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
            return provider.canDropChunk(x, z) && (!manager.world.isSpawnChunk(x, z) || !PlayerSpawnLogic.isInitialSpawnDimension(provider));
        }

        // Helper.
        @Nonnull
        private static OptionalInt findValidDimension(@Nonnull final EntityPlayerMP player, @Nonnull final boolean[] spawnPointObstructed, @Nonnull final Integer[] dimensions, @Nonnull final IntPredicate filter) {
            return Arrays.stream(dimensions).mapToInt(Integer::intValue).filter(filter.and(dimension -> {
                @Nullable final BlockPos bedPos = player.getBedLocation(dimension);
                if(bedPos == null) return false;

                @Nullable final World world = player.server.getWorld(dimension);
                final boolean isValid = world != null && EntityPlayer.getBedSpawnLocation(world, bedPos, player.isSpawnForced(dimension)) != null;

                if(!isValid) spawnPointObstructed[0] = true;
                return isValid;
            })).findFirst();
        }

        public static int getRespawnDimension(@Nonnull final EntityPlayerMP player, @Nullable final World world, final int dimension, final boolean conqueredEnd) {
            // Always respect any mod-set respawn dimension override, except the End's exit portal, which shouldn't be treated like normal respawning.
            if(player.hasSpawnDimension() && (!conqueredEnd || player.dimension != player.getSpawnDimension())) return player.getSpawnDimension();
            final boolean canRespawnHere = !conqueredEnd && world != null && PlayerSpawnLogic.canSpawnInDimension(world.provider, player);

            final int defaultDimension;
            if(canRespawnHere && player.dimension != 0) defaultDimension = player.dimension;
            else { // Force the End's exit portal to warp into a non-End "player spawn dimension".
                final int initialDimension = PlayerSpawnLogic.getInitialSpawnDimension(player.getGameProfile());
                defaultDimension = conqueredEnd && player.dimension == initialDimension ? 0 : initialDimension;
            }

            // No special dimension is set, send to spawn point dimension or to the default spawn dimension.
            if(canRespawnHere || dimension == 0 || dimension == defaultDimension) {
                @Nonnull final boolean[] spawnPointObstructed = new boolean[1];
                final int respawnDimension = (!conqueredEnd && player.getBedLocation() != null ? findValidDimension(player, spawnPointObstructed, new Integer[] {player.dimension}, id -> true) : OptionalInt.empty())
                        .orElseGet(() -> findValidDimension(player, spawnPointObstructed, DimensionManager.getStaticDimensionIDs(), conqueredEnd ? id -> id != player.dimension : id -> true).orElse(defaultDimension));

                // PlayerList only sends the "bed is obstructed" warning for beds in the same dimension, this fixes that.
                if(spawnPointObstructed[0]) {
                    @Nullable final World respawnWorld = player.server.getWorld(respawnDimension);
                    if(respawnWorld == null && player.getBedLocation(0) == null || player.getBedLocation(respawnDimension) == null) player.connection.sendPacket(new SPacketChangeGameState(0, 0));
                }

                return respawnDimension;
            }

            // Special logic found.
            else return dimension;
        }

        public static int getSpawnDimension(@Nonnull final EntityPlayerMP player, final int fallback) {
            return player.hasSpawnDimension() ? player.getSpawnDimension() : fallback;
        }

        public static boolean isBonusChestEnabled(@Nonnull final WorldSettings settings, @Nonnull final World world) {
            return settings.isBonusChestEnabled() && world.provider.getDimension() == 0;
        }
    }
}
