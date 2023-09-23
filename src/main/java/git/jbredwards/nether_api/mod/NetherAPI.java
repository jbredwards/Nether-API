/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.compat.betternether.BetterNetherHandler;
import git.jbredwards.nether_api.mod.common.compat.journey_into_the_light.JITLHandler;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
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
    public static final String MODID = "nether_api", NAME = "Nether API", VERSION = "1.3.0", DEPENDENCIES =
            // Minimum supported mod versions, since earlier versions may cause ASM problems
            "after:betternether@[0.1.8.6,);after:biomesoplenty@[7.0.1.2444,);after:journey@1.0.6.11;after:netherex@[2.2.5,);after:stygian@[1.0.4,);";

    // Mod Compatibility
    public static final boolean isBetterNetherLoaded = Loader.isModLoaded("betternether");
    public static final boolean isBiomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
    public static final boolean isJourneyIntoTheLightLoaded = Loader.isModLoaded("journey");
    public static final boolean isNetherExLoaded = Loader.isModLoaded("netherex");
    public static final boolean isStygianEndLoaded = Loader.isModLoaded("stygian");

    // Register actual biomes for pseudo biomes
    @Mod.EventHandler
    static void construct(@Nonnull FMLConstructionEvent event) {
        if(isBetterNetherLoaded) MinecraftForge.EVENT_BUS.register(BetterNetherHandler.class);
        if(isJourneyIntoTheLightLoaded) MinecraftForge.EVENT_BUS.register(JITLHandler.class);
    }

    // Fix BetterNether firefly
    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) {
        if(isBetterNetherLoaded) BetterNetherHandler.init();
        if(isJourneyIntoTheLightLoaded) JITLHandler.init();
    }

    // Register dimension overrides
    @Mod.EventHandler
    static void serverAboutToStart(@Nonnull FMLServerAboutToStartEvent event) {
        DimensionManager.getProviderType(DimensionType.NETHER.getId()).clazz = WorldProviderNether.class;
        //TODO DimensionManager.getProviderType(DimensionType.THE_END.getId()).clazz = WorldProviderTheEnd.class;
    }

    // Ensure all registries are cleared before another world is loaded
    @Mod.EventHandler
    static void serverStopping(@Nonnull FMLServerStoppingEvent event) {
        INetherAPIRegistry.REGISTRIES.forEach(INetherAPIRegistry::clear);
    }
}
