/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.nethercraft;

import com.legacy.nethercraft.blocks.BlocksNether;
import com.legacy.nethercraft.entities.block.EntityGhastBomb;
import com.legacy.nethercraft.entities.hostile.*;
import com.legacy.nethercraft.entities.item.EntityLavaBoat;
import com.legacy.nethercraft.entities.item.EntityNPainting;
import com.legacy.nethercraft.entities.projectile.*;
import com.legacy.nethercraft.entities.tribal.EntityTribalTrainee;
import com.legacy.nethercraft.entities.tribal.EntityTribalWarrior;
import com.legacy.nethercraft.world.NetherGenMinable;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class NethercraftHandler
{
    @Nonnull
    static final Field
            TRACKING_RANGE_FIELD = ObfuscationReflectionHelper.findField(EntityRegistry.EntityRegistration.class, "trackingRange"),
            UPDATE_FREQUENCY_FIELD = ObfuscationReflectionHelper.findField(EntityRegistry.EntityRegistration.class, "updateFrequency"),
            SENDS_VELOCITY_UPDATES_FIELD = ObfuscationReflectionHelper.findField(EntityRegistry.EntityRegistration.class, "sendsVelocityUpdates");

    public static BiomeNethercraft GLOWING_GROVE;
    public static int // non-hardcoded for modpack devs, these can be changed through the use of GroovyScript
            foulitePerChunk = 20, fouliteOreSize = 14, fouliteMinHeight = 10, fouliteMaxHeight = 118,
            neridiumPerChunk = 8, neridiumOreSize = 14, neridiumMinHeight = 10, neridiumMaxHeight = 118,
            liniumPerChunk = 5, liniumOreSize = 4, liniumMinHeight = 10, liniumMaxHeight = 118,
            pyridiumPerChunk = 4, pyridiumOreSize = 6, pyridiumMinHeight = 10, pyridiumMaxHeight = 118,
            wPerChunk = 2, wOreSize = 4, wMinHeight = 10, wMaxHeight = 118;

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        registry.registerBiome(GLOWING_GROVE, NetherAPIConfig.Nethercraft.glowingGroveWeight);
    }

    public static void init() {
        // asm is used to change all other nethercraft mobs to spawn in glowing grove biome
        // in future versions, this mob is able to spawn in nether wastes
        EntityRegistry.addSpawn(EntityCamouflageSpider.class, 60, 1, 3, EnumCreatureType.MONSTER, Biomes.HELL);

        // ----------------------------
        // fix broken entity networking
        // ----------------------------

        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityDarkZombie.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityLavaSlime.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityCamouflageSpider.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityImp.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityTribalTrainee.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityTribalWarrior.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityBloodyZombie.class, false)).ifPresent(NethercraftHandler::setSendsVelocityUpdates);

        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityGhastBomb.class, false)).ifPresent(entry -> setInt(UPDATE_FREQUENCY_FIELD, entry, 10));
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityLiniumArrow.class, false)).ifPresent(entry -> setInt(UPDATE_FREQUENCY_FIELD, entry, 20));
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityNeridiumArrow.class, false)).ifPresent(entry -> setInt(UPDATE_FREQUENCY_FIELD, entry, 20));
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityNetherArrow.class, false)).ifPresent(entry -> setInt(UPDATE_FREQUENCY_FIELD, entry, 20));
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityPyridiumArrow.class, false)).ifPresent(entry -> setInt(UPDATE_FREQUENCY_FIELD, entry, 20));

        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntitySlimeEggs.class, false)).ifPresent(entry -> { setInt(UPDATE_FREQUENCY_FIELD, entry, 10); setSendsVelocityUpdates(entry); });
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityLavaBoat.class, false)).ifPresent(entry -> { setInt(TRACKING_RANGE_FIELD, entry, 80); setSendsVelocityUpdates(entry); });
        Optional.ofNullable(EntityRegistry.instance().lookupModSpawn(EntityNPainting.class, false)).ifPresent(entry -> { setInt(TRACKING_RANGE_FIELD, entry, 160); setInt(UPDATE_FREQUENCY_FIELD, entry, Integer.MAX_VALUE); });
    }

    static void setInt(@Nonnull Field field, @Nonnull Object obj, int value) { try { field.setInt(obj, value); } catch(final IllegalAccessException e) { throw new RuntimeException(e); } }
    static void setSendsVelocityUpdates(@Nonnull Object obj) { try { SENDS_VELOCITY_UPDATES_FIELD.setBoolean(obj, true); } catch(final IllegalAccessException e) { throw new RuntimeException(e); } }

    @SubscribeEvent
    static void registerBiomes(@Nonnull RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(GLOWING_GROVE = new BiomeNethercraft());
    }

    @SubscribeEvent
    static void generateOres(@Nonnull DecorateBiomeEvent.Pre event) {
        if(event.getWorld().provider.getDimensionType() == DimensionType.NETHER) {
            GLOWING_GROVE.decorator.chunkPos = event.getPos();
            GLOWING_GROVE.decorator.genStandardOre1(event.getWorld(), event.getRand(), foulitePerChunk, new NetherGenMinable(BlocksNether.foulite_ore.getDefaultState(), fouliteOreSize), fouliteMinHeight, fouliteMaxHeight);
            GLOWING_GROVE.decorator.genStandardOre1(event.getWorld(), event.getRand(), neridiumPerChunk, new NetherGenMinable(BlocksNether.neridium_ore.getDefaultState(), neridiumOreSize), neridiumMinHeight, neridiumMaxHeight);
            GLOWING_GROVE.decorator.genStandardOre1(event.getWorld(), event.getRand(), liniumPerChunk, new NetherGenMinable(BlocksNether.linium_ore.getDefaultState(), liniumOreSize), liniumMinHeight, liniumMaxHeight);
            GLOWING_GROVE.decorator.genStandardOre1(event.getWorld(), event.getRand(), pyridiumPerChunk, new NetherGenMinable(BlocksNether.pyridium_ore.getDefaultState(), pyridiumOreSize), pyridiumMinHeight, pyridiumMaxHeight);
            GLOWING_GROVE.decorator.genStandardOre1(event.getWorld(), event.getRand(), wPerChunk, new NetherGenMinable(BlocksNether.w_ore.getDefaultState(), wOreSize), wMinHeight, wMaxHeight);
        }
    }

    // functionality backported from future versions of Nethercraft, to prevent normal zombies from generating in the nether
    @SubscribeEvent(priority = EventPriority.LOW)
    static void summonDarkZombie(@Nonnull ZombieEvent.SummonAidEvent event) {
        if(event.getResult() == Event.Result.DEFAULT && event.getSummonChance() > 0
        && event.getWorld().provider.getDimensionType() == DimensionType.NETHER
        && event.getWorld().getDifficulty() == EnumDifficulty.HARD
        && event.getWorld().getGameRules().getBoolean("doMobSpawning")) {
            if(event.getSummoner().getRNG().nextDouble() > event.getSummonChance()) event.setResult(Event.Result.DENY);
            event.setCustomSummonedAid(new EntityDarkZombie(event.getWorld()));
            event.setResult(Event.Result.ALLOW);
        }
    }
}
