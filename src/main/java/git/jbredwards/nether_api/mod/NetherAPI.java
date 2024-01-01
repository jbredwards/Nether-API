/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.compat.betternether.BetterNetherHandler;
import git.jbredwards.nether_api.mod.common.compat.journey_into_the_light.JITLHandler;
import git.jbredwards.nether_api.mod.common.compat.nethercraft.NethercraftHandler;
import git.jbredwards.nether_api.mod.common.compat.stygian_end.StygianEndHandler;
import git.jbredwards.nether_api.mod.common.network.MessageTeleportFX;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
            "after:betternether@[0.1.8.6,);after:biomesoplenty@[7.0.1.2444,);after:journey@1.0.6.11;after:natura@1.12.2-4.3.2.69;after:nethercraft@1.0.2;after:netherex@2.2.5;after:stygian@[1.0.4,);";

    // Mod Compatibility
    public static final boolean isBetterNetherLoaded = Loader.isModLoaded("betternether");
    public static final boolean isBiomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
    public static final boolean isJourneyIntoTheLightLoaded = Loader.isModLoaded("journey");
    public static final boolean isNethercraftLoaded = Loader.isModLoaded("nethercraft");
    public static final boolean isNetherExLoaded = Loader.isModLoaded("netherex");
    public static final boolean isStygianEndLoaded = Loader.isModLoaded("stygian");

    // Packet handler
    @Nonnull
    public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    public static int messageId = 0;

    // Register actual biomes for pseudo biomes
    @Mod.EventHandler
    static void construct(@Nonnull final FMLConstructionEvent event) {
        if(isBetterNetherLoaded) MinecraftForge.EVENT_BUS.register(BetterNetherHandler.class);
        if(isJourneyIntoTheLightLoaded) MinecraftForge.EVENT_BUS.register(JITLHandler.class);
        if(isNethercraftLoaded) MinecraftForge.EVENT_BUS.register(NethercraftHandler.class);
    }

    // Register packets
    @Mod.EventHandler
    static void preInit(@Nonnull final FMLPreInitializationEvent event) {
        WRAPPER.registerMessage(MessageTeleportFX.Handler.INSTANCE, MessageTeleportFX.class, messageId++, Side.CLIENT);
    }

    // Fix some misc mod issues (like the BetterNether firefly spawn biomes)
    @Mod.EventHandler
    static void init(@Nonnull final FMLInitializationEvent event) {
        if(isBetterNetherLoaded) BetterNetherHandler.init();
        if(isJourneyIntoTheLightLoaded) JITLHandler.init();
        if(isNethercraftLoaded) NethercraftHandler.init();
        if(isStygianEndLoaded) StygianEndHandler.init();
    }

    // Fix some misc mod client issues (like the Stygian End leaves rendering badly in the end)
    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void initClient(@Nonnull final FMLInitializationEvent event) {
        if(isStygianEndLoaded) StygianEndHandler.initClient();
    }

    // Register dimension overrides
    @Mod.EventHandler
    static void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) {
        DimensionManager.getProviderType(DimensionType.NETHER.getId()).clazz = WorldProviderNether.class;
        DimensionManager.getProviderType(DimensionType.THE_END.getId()).clazz = WorldProviderTheEnd.class;
    }

    // Ensure all registries are cleared before another world is loaded
    @Mod.EventHandler
    static void serverStopping(@Nonnull final FMLServerStoppingEvent event) {
        INetherAPIRegistry.REGISTRIES.forEach(INetherAPIRegistry::clear);
    }
}
