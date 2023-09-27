/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.config;

import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Config(modid = NetherAPI.MODID, name = "nether_api/vanilla")
@Mod.EventBusSubscriber(modid = NetherAPI.MODID)
public final class NetherAPIConfig
{
    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.endWeight")
    public static int endWeight = 100;

    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.hellWeight")
    public static int hellWeight = 30;

    @Config(modid = NetherAPI.MODID, name = "nether_api/biomes_o_plenty")
    public static final class BOP
    {
        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.bop.dependentHellBiomes")
        public static boolean dependentBOPHellBiomes = true;
    }

    @Config(modid = NetherAPI.MODID, name = "nether_api/journey_into_the_light")
    public static final class JITL
    {
        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.jitl.bloodForestWeight")
        public static int bloodForestWeight = 30;

        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.jitl.earthenSeepWeight")
        public static int earthenSeepWeight = 30;

        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.jitl.heatSandsWeight")
        public static int heatSandsWeight = 30;
    }

    @Config(modid = NetherAPI.MODID, name = "nether_api/nethercraft")
    public static final class Nethercraft
    {
        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.nethercraft.glowingGroveWeight")
        public static int glowingGroveWeight = 30;
    }

    @Config(modid = NetherAPI.MODID, name = "nether_api/stygain_end")
    public static final class StygainEnd
    {

    }

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(NetherAPI.MODID)) ConfigManager.sync(NetherAPI.MODID, Config.Type.INSTANCE);
    }
}
