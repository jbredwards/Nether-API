/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod;

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.mod.common.compat.betternether.BetterNetherHandler;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = NetherAPI.MODID, name = NetherAPI.NAME, version = NetherAPI.VERSION, dependencies = NetherAPI.DEPENDENCIES)
public final class NetherAPI
{
    // Mod Constants
    @Nonnull
    public static final String MODID = "nether_api", NAME = "Nether API", VERSION = "1.2.0", DEPENDENCIES =
            // Minimum supported mod versions, since earlier versions may cause ASM problems
            "after:betternether@[0.1.8.6,);after:biomesoplenty@[7.0.1.2444,);after:netherex@[2.2.5,);";

    // Mod Compatibility
    public static final boolean isBetterNetherLoaded = Loader.isModLoaded("betternether");
    public static final boolean isBiomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
    public static final boolean isNetherExLoaded = Loader.isModLoaded("netherex");
    // TODO public static final boolean isStygianEndLoaded = Loader.isModLoaded("stygian");

    // Register BetterNether biomes
    @Mod.EventHandler
    static void construct(@Nonnull FMLConstructionEvent event) {
        if(isBetterNetherLoaded) MinecraftForge.EVENT_BUS.register(BetterNetherHandler.class);
    }

    // Fix BetterNether firefly
    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) {
        if(isBetterNetherLoaded) BetterNetherHandler.removeOrFixEntitySpawnBiomes();
    }

    // Register Nether handler
    @Mod.EventHandler
    static void serverStating(@Nonnull FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.Nether(NetherAPIRegistry.NETHER, event.getServer()));
        DimensionManager.getProviderType(DimensionType.NETHER.getId()).clazz = WorldProviderNether.class;

        MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.End(NetherAPIRegistry.THE_END, event.getServer()));
        //TODO DimensionManager.getProviderType(DimensionType.THE_END.getId()).clazz = WorldProviderTheEnd.class;
    }

    // Unregister Nether handler
    @Mod.EventHandler
    static void serverStopping(@Nonnull FMLServerStoppingEvent event) {
        NetherAPIRegistry.NETHER.clear();
        NetherAPIRegistry.THE_END.clear();
    }
}
