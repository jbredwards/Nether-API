/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.voidislandcontrol;

import com.bartz24.voidislandcontrol.EventHandler;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class VoidIslandControlHandler
{
    public static boolean isMapFeaturesEnabled(@Nonnull final World world) {
        if(isVoid(world)) return world.provider.isNether() ? ConfigOptions.worldGenSettings.netherVoidStructures : ConfigOptions.worldGenSettings.endVoidStructures;
        return world.getWorldInfo().isMapFeaturesEnabled();
    }

    public static boolean isVoid(@Nonnull final World world) {
        if(NetherAPI.isVoidIslandControlLoaded) return world.provider.isNether() ? ConfigOptions.worldGenSettings.netherVoid : ConfigOptions.worldGenSettings.endVoid;
        return false;
    }

    @Nonnull
    public static BlockPos getEndSpawnPos(@Nonnull final BlockPos fallback) {
        if(NetherAPI.isVoidIslandControlLoaded) return new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0);
        return fallback;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void createStartingIsland(@Nonnull final PlayerEvent.PlayerRespawnEvent event) {
        // Ensure starting island is created even if the player does not initially spawn in "baseDimension".
        new EventHandler().playerLogin(new PlayerEvent.PlayerLoggedInEvent(event.player));
    }
}
