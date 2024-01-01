/*
 * Copyright (c) 2023-2024. jbredwards
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
    @Config.LangKey("config.nether_api.endCaves")
    public static boolean endCaves = true;

    @Config.LangKey("config.nether_api.hellCaves")
    public static boolean hellCaves = true;

    @Deprecated // DO NOT USE THIS!!! INSTEAD CALL -> world.getActualHeight()
    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.tallNether")
    public static boolean tallNether = false;

    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.endWeight")
    public static int endWeight = 100;

    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.hellWeight")
    public static int hellWeight = 8;

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
        public static int bloodForestWeight = 8;

        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.jitl.earthenSeepWeight")
        public static int earthenSeepWeight = 5;

        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.jitl.heatSandsWeight")
        public static int heatSandsWeight = 6;
    }

    @Config(modid = NetherAPI.MODID, name = "nether_api/nethercraft")
    public static final class Nethercraft
    {
        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.nethercraft.glowingGroveWeight")
        public static int glowingGroveWeight = 4;
    }

    @Config(modid = NetherAPI.MODID, name = "nether_api/stygian_end")
    public static final class StygianEnd
    {
        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.stygian_end.endVolcanoWeight")
        public static int endVolcanoWeight = 100;

        @Config.LangKey("config.nether_api.compat.stygian_end.endVolcanoChorusPlants")
        public static boolean endVolcanoChorusPlants = false;

        @Config.LangKey("config.nether_api.compat.stygian_end.endVolcanoEndCity")
        public static boolean endVolcanoEndCity = false;

        @Config.RequiresWorldRestart
        @Config.LangKey("config.nether_api.compat.stygian_end.endJungleWeight")
        public static int endJungleWeight = 100;

        @Config.LangKey("config.nether_api.compat.stygian_end.endJungleChorusPlants")
        public static boolean endJungleChorusPlants = false;

        @Config.LangKey("config.nether_api.compat.stygian_end.endJungleEndCity")
        public static boolean endJungleEndCity = false;

        @Config.RequiresMcRestart
        @Config.LangKey("config.nether_api.compat.stygian_end.wideEnderCanopyGen")
        public static boolean wideEnderCanopyGen = false;
    }

    @SubscribeEvent
    static void sync(@Nonnull final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(NetherAPI.MODID)) ConfigManager.sync(NetherAPI.MODID, Config.Type.INSTANCE);
    }
}
