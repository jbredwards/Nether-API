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

package git.jbredwards.nether_api.mod.asm.transformers.modded.voidislandcontrol;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't crash the event handler if the player spawns in another dimension
 * @author jbred
 *
 */
public final class TransformerEventHandler implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("onPlayerRespawn"), (method, insn) -> {
            /*
             * Old code:
             * if (player.getEntityWorld().getWorldInfo().getTerrainType() instanceof WorldTypeVoid && (player.getBedLocation() == null || EntityPlayer.getBedSpawnLocation(player.getEntityWorld(), player.getBedLocation(), true) == null))
             * {
             *     ...
             * }
             *
             * New code:
             * // Don't crash the event handler if the player spawns in another dimension.
             * if (Hooks.getTerrainType(player.getEntityWorld().getWorldInfo(), player) instanceof WorldTypeVoid && (player.getBedLocation() == null || EntityPlayer.getBedSpawnLocation(player.getEntityWorld(), player.getBedLocation(), true) == null))
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getTerrainType" : "func_76067_t")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insertBefore(insn, genHookMethod("getTerrainType", "(Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/world/WorldType;"));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static WorldType getTerrainType(@Nonnull final WorldInfo info, @Nonnull final EntityPlayer player) {
            return player.dimension == ConfigOptions.worldGenSettings.baseDimension ? info.getTerrainType() : WorldType.DEFAULT;
        }
    }
}
