package git.jbredwards.nether_api.mod;

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = NetherAPI.MODID, name = NetherAPI.NAME, version = NetherAPI.VERSION)
public final class NetherAPI
{
    // Mod Constants
    @Nonnull
    public static final String MODID = "nether_api", NAME = "Nether API", VERSION = "1.0.0";

    // Mod Compatibility
    public static final boolean isBetterNetherLoaded = Loader.isModLoaded("betternether");
    public static final boolean isBiomesOPlentyLoaded = Loader.isModLoaded("biomesoplenty");
    public static final boolean isNetherExLoaded = Loader.isModLoaded("netherex");
    public static final boolean isStygianEndLoaded = Loader.isModLoaded("stygian");

    // Register Nether Handler
    @Mod.EventHandler
    static void serverStating(@Nonnull FMLServerStartingEvent event) {
        MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.Nether(NetherAPIRegistry.NETHER, event.getServer()));
        DimensionManager.getProviderType(DimensionType.NETHER.getId()).clazz = WorldProviderNether.class;

        MinecraftForge.EVENT_BUS.post(new NetherAPIRegistryEvent.End(NetherAPIRegistry.THE_END, event.getServer()));
        //TODO DimensionManager.getProviderType(DimensionType.THE_END.getId()).clazz = WorldProviderTheEnd.class;
    }

    // Unregister Nether Handler
    @Mod.EventHandler
    static void serverStopping(@Nonnull FMLServerStoppingEvent event) {
        NetherAPIRegistry.NETHER.clear();
        NetherAPIRegistry.THE_END.clear();
    }
}
