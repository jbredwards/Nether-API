package git.jbredwards.nether_api.mod.common.config;

import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

@Config(modid = NetherAPI.MODID)
@Mod.EventBusSubscriber(modid = NetherAPI.MODID)
public final class NetherAPIConfig
{
    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.compat.bop.dependentHellBiomes")
    public static boolean dependentBOPHellBiomes = true;

    @Config.RequiresWorldRestart
    @Config.LangKey("config.nether_api.hellWeight")
    public static int hellWeight = 30;

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(NetherAPI.MODID)) ConfigManager.sync(NetherAPI.MODID, Config.Type.INSTANCE);
    }
}
