/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.betternether;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Biomes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import paulevs.betternether.biomes.BiomeRegister;
import paulevs.betternether.biomes.NetherBiome;
import paulevs.betternether.config.ConfigLoader;
import paulevs.betternether.entities.EntityFirefly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.ObjIntConsumer;

/**
 * This class has compatibility with both the legacy and forked versions of BetterNether
 * @author jbred
 *
 */
public final class BetterNetherHandler
{
    @Nonnull static final List<BiomeBetterNether> BIOMES = new LinkedList<>();
    @Nonnull static final Map<NetherBiome, BiomeBetterNether> BIOME_LOOKUP = new HashMap<>();
    @Nullable static Field legacyEnabledBiomes;

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        for(final BiomeBetterNether biome : BIOMES) {
            //don't register the internal fallback biome for generation
            if(biome.netherBiome != BiomeRegister.BIOME_EMPTY_NETHER) {
                //don't register sub-biomes or edge biomes for individual generation
                if(biome.netherBiome == BiomeRegister.BIOME_MUSHROOM_FOREST_EDGE) break;
                else registry.registerBiome(biome, getWeight(biome));
            }
        }
    }

    @Nonnull
    public static BiomeBetterNether getBiomeFromLookup(@Nonnull NetherBiome netherBiome) {
        final BiomeBetterNether biome = BIOME_LOOKUP.get(netherBiome);
        if(biome != null) return biome;

        //should never pass
        throw new IllegalStateException("No Biome found for BetterNether: {" + netherBiome.getName() + '}');
    }

    public static int getWeight(@Nonnull BiomeBetterNether biome) {
        if(biome.cachedWeight == -1) {
            //using a forked version of the mod
            if(biome.netherBiome instanceof WeightedRandom.Item) biome.cachedWeight = ConfigLoader.mustInitBiome(biome.netherBiome) ? biome.netherBiome.itemWeight : 0;

            //using the original mod
            else {
                if(legacyEnabledBiomes == null) legacyEnabledBiomes = ObfuscationReflectionHelper.findField(ConfigLoader.class, "registerBiomes");
                try { biome.cachedWeight = ((boolean[])legacyEnabledBiomes.get(null))[biome.netherBiomeId] ? 1 : 0; }

                //should never pass
                catch(final IllegalAccessException e) { throw new RuntimeException(e); }
            }
        }

        return biome.cachedWeight;
    }

    @SubscribeEvent
    static void registerBiomes(@Nonnull RegistryEvent.Register<Biome> event) {
        final ObjIntConsumer<NetherBiome> registerAction = (netherBiome, netherBiomeId) -> {
            final BiomeBetterNether biome = new BiomeBetterNether(netherBiome, netherBiomeId);
            event.getRegistry().register(biome);

            BIOMES.add(biome);
            BIOME_LOOKUP.put(netherBiome, biome);
            BiomeDictionary.addTypes(biome, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY);
        };

        //register biomes in the correct order
        registerAction.accept(BiomeRegister.BIOME_EMPTY_NETHER, 0);
        registerAction.accept(BiomeRegister.BIOME_GRAVEL_DESERT, 1);
        registerAction.accept(BiomeRegister.BIOME_NETHER_JUNGLE, 2);
        registerAction.accept(BiomeRegister.BIOME_WART_FOREST, 3);
        registerAction.accept(BiomeRegister.BIOME_GRASSLANDS, 4);
        registerAction.accept(BiomeRegister.BIOME_MUSHROOM_FOREST, 5);
        registerAction.accept(BiomeRegister.BIOME_MUSHROOM_FOREST_EDGE, 6);
        registerAction.accept(BiomeRegister.BIOME_WART_FOREST_EDGE, 7);
        registerAction.accept(BiomeRegister.BIOME_BONE_REEF, 8);
        registerAction.accept(BiomeRegister.BIOME_POOR_GRASSLANDS, 9);
    }

    //exists because this mod adds BetterNether biomes as real biomes
    public static void removeOrFixEntitySpawnBiomes() {
        //fix fireflies
        Biomes.HELL.getSpawnableList(EnumCreatureType.AMBIENT).removeIf(entry -> entry.entityClass == EntityFirefly.class);
        EntityRegistry.addSpawn(EntityFirefly.class, 100, 5, 10, EnumCreatureType.AMBIENT,
                getBiomeFromLookup(BiomeRegister.BIOME_GRASSLANDS), getBiomeFromLookup(BiomeRegister.BIOME_NETHER_JUNGLE)
        );

        //remove ghasts from biomes that have cacti, as to prevent lots of really annoying damage sounds from playing
        getBiomeFromLookup(BiomeRegister.BIOME_GRAVEL_DESERT).getSpawnableList(EnumCreatureType.MONSTER)
                .removeIf(entry -> EntityGhast.class.isAssignableFrom(entry.entityClass));
    }
}
