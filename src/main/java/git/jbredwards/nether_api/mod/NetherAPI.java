package git.jbredwards.nether_api.mod;

import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import javax.annotation.Nonnull;

@Mod(modid = NetherAPI.MODID, name = NetherAPI.NAME, version = NetherAPI.VERSION)
public final class NetherAPI
{
    // Mod Constants
    @Nonnull
    public static final String MODID = "nether_api", NAME = "Nether API", VERSION = "1.0.0";

    // Mod Compatibility
    public static final boolean isBetterNetherLoaded = Loader.isModLoaded();
    public static final boolean isBiomesOPlentyLoaded = Loader.isModLoaded();
    public static final boolean isNetherExLoaded = Loader.isModLoaded();
    public static final boolean isPrimalCoreLoaded = Loader.isModLoaded();

    // Register Nether Biome Handler
    @Mod.EventHandler
    static void serverStating(@Nonnull FMLServerStartingEvent event) {
        DimensionType.getById(DimensionType.NETHER.getId()).clazz = WorldProviderNether.class;
    }

    // Unregister Nether Biome Handler
    @Mod.EventHandler
    static void serverStopping(@Nonnull FMLServerStoppingEvent event) {

    }
}
