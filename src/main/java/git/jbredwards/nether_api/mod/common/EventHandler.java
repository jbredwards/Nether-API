/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common;

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.betternether.BetterNetherHandler;
import git.jbredwards.nether_api.mod.common.compat.biomesoplenty.BiomesOPlentyHandler;
import git.jbredwards.nether_api.mod.common.compat.journey_into_the_light.JITLHandler;
import git.jbredwards.nether_api.mod.common.compat.nethercraft.NethercraftHandler;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.compat.stygian_end.StygianEndHandler;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.BlockTorch;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
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
    static void registerHardcodedEnd(@Nonnull NetherAPIRegistryEvent.End event) {
        //built-in supported mods, other mods must use the event themselves
        if(NetherAPI.isStygianEndLoaded) StygianEndHandler.registerBiomes(event.registry);
        //vanilla
        event.registry.registerBiome(Biomes.SKY, NetherAPIConfig.endWeight);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void registerHardcodedNether(@Nonnull NetherAPIRegistryEvent.Nether event) {
        //built-in supported mods, other mods must use the event themselves
        if(NetherAPI.isBetterNetherLoaded) BetterNetherHandler.registerBiomes(event.registry);
        if(NetherAPI.isBiomesOPlentyLoaded) BiomesOPlentyHandler.registerBiomes(event.registry, event.world);
        if(NetherAPI.isJourneyIntoTheLightLoaded) JITLHandler.registerBiomes(event.registry);
        if(NetherAPI.isNethercraftLoaded) NethercraftHandler.registerBiomes(event.registry);
        if(NetherAPI.isNetherExLoaded) NetherExHandler.registerBiomes(event.registry);
        //vanilla
        event.registry.registerBiome(Biomes.HELL, NetherAPIConfig.hellWeight);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void handleDragonResistantBlocks(@Nonnull LivingDestroyBlockEvent event) {
        if(event.getEntity() instanceof EntityDragon) {
            // prevent the ender dragon from breaking parts of the exit portal
            if(event.getState().getBlock() instanceof BlockTorch) event.setCanceled(true);
            // all blocks with the blast resistance of at least obsidian should be dragon-resistant
            else {
                final World world = event.getEntity().getEntityWorld();
                final BlockPos pos = event.getPos();
                final Explosion explosion = new Explosion(world, event.getEntity(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.5f, false, true);
                if(event.getEntity().getExplosionResistance(explosion, world, pos, event.getState()) >= 1200) event.setCanceled(true);
            }
        }
    }
}
