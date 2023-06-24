package git.jbredwards.nether_api.mod.common;

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.betternether.BetterNetherHandler;
import git.jbredwards.nether_api.mod.common.compat.biomesoplenty.BiomesOPlentyHandler;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.init.Biomes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = NetherAPI.MODID)
final class EventHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void registerHardcodedNether(@Nonnull NetherAPIRegistryEvent.Nether event) {
        //vanilla
        event.registry.registerBiome(Biomes.HELL, NetherAPIConfig.hellWeight);

        //built-in supported mods, other mods must use the event themselves
        if(NetherAPI.isBetterNetherLoaded) BetterNetherHandler.registerBiomes(event.registry);
        if(NetherAPI.isBiomesOPlentyLoaded) BiomesOPlentyHandler.registerBiomes(event.registry, event.server);
        if(NetherAPI.isNetherExLoaded) NetherExHandler.registerBiomes(event.registry);
    }
}
