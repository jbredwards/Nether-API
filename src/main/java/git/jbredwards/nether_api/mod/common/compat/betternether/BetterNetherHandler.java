/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.betternether;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
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
import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * This class has compatibility with both the legacy and forked versions of BetterNether
 * @author jbred
 *
 */
public final class BetterNetherHandler
{
    @Nonnull static final Set<NetherBiome> GEN_BIOMES = new HashSet<>();
    @Nonnull static final Map<NetherBiome, BiomeBetterNether> BIOME_LOOKUP = new HashMap<>();

    @SuppressWarnings("ConstantValue")
    public static boolean isModern() {
        return WeightedRandom.Item.class.isAssignableFrom(NetherBiome.class);
    }

    public static void registerBiomes(@Nonnull final INetherAPIRegistry registry) {
        GEN_BIOMES.forEach(netherBiome -> {
            @Nonnull final BiomeBetterNether biome = BIOME_LOOKUP.get(netherBiome);
            registry.registerBiome(biome, getWeight(biome));
        });
    }

    @Nonnull
    public static BiomeBetterNether getBiomeFromLookup(@Nonnull final NetherBiome netherBiome) {
        @Nonnull final BiomeBetterNether biome = BIOME_LOOKUP.get(netherBiome);
        if(biome != null) return biome;

        // should never pass
        throw new IllegalStateException("No Biome found for BetterNether: {" + netherBiome.getName() + '}');
    }

    public static int getWeight(@Nonnull final BiomeBetterNether biome) {
        if(biome.cachedWeight == -1) {
            // using a forked version of the mod
            if(isModern()) biome.cachedWeight = ConfigLoader.mustInitBiome(biome.netherBiome) ? biome.netherBiome.itemWeight : 0;

            // using the original mod, config disable already handled via biome != null in registerBiomes
            else biome.cachedWeight = 1;
        }

        return biome.cachedWeight;
    }

    @SubscribeEvent
    static void registerBiomes(@Nonnull final RegistryEvent.Register<Biome> event) {
        @Nonnull final ObjIntConsumer<NetherBiome> registerAction = (netherBiome, netherBiomeId) -> {
            if(netherBiome == null) return;

            @Nonnull final BiomeBetterNether biome = new BiomeBetterNether(netherBiome, netherBiomeId);
            event.getRegistry().register(biome);

            BIOME_LOOKUP.put(netherBiome, biome);
            BiomeDictionary.addTypes(biome, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY);
        };

        // register biomes for gen
        if(BiomeRegister.BIOME_GRAVEL_DESERT != null) GEN_BIOMES.add(BiomeRegister.BIOME_GRAVEL_DESERT);
        if(BiomeRegister.BIOME_NETHER_JUNGLE != null) GEN_BIOMES.add(BiomeRegister.BIOME_NETHER_JUNGLE);
        if(BiomeRegister.BIOME_WART_FOREST != null) GEN_BIOMES.add(BiomeRegister.BIOME_WART_FOREST);
        if(BiomeRegister.BIOME_GRASSLANDS != null) GEN_BIOMES.add(BiomeRegister.BIOME_GRASSLANDS);
        if(BiomeRegister.BIOME_MUSHROOM_FOREST != null) GEN_BIOMES.add(BiomeRegister.BIOME_MUSHROOM_FOREST);

        // register biomes
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

    // exists because this mod adds BetterNether biomes as real biomes
    public static void init() {
        // ensure netherrack is set as a valid terrain block (needed for TransformerBetterNetherPlants)
        if(isModern()) ObfuscationReflectionHelper.<Set<Block>, ConfigLoader>getPrivateValue(ConfigLoader.class, null, "NETHER_TERRAIN").add(Blocks.NETHERRACK);

        // fix fireflies
        Biomes.HELL.getSpawnableList(EnumCreatureType.AMBIENT).removeIf(entry -> entry.entityClass == EntityFirefly.class);
        if(BiomeRegister.BIOME_GRASSLANDS != null) EntityRegistry.addSpawn(EntityFirefly.class, 100, 5, 10, EnumCreatureType.AMBIENT, getBiomeFromLookup(BiomeRegister.BIOME_GRASSLANDS));
        if(BiomeRegister.BIOME_NETHER_JUNGLE != null) EntityRegistry.addSpawn(EntityFirefly.class, 100, 5, 10, EnumCreatureType.AMBIENT, getBiomeFromLookup(BiomeRegister.BIOME_NETHER_JUNGLE));

        // remove ghasts from biomes that have cacti, as to prevent lots of really annoying damage sounds from playing
        if(BiomeRegister.BIOME_GRAVEL_DESERT != null)
            getBiomeFromLookup(BiomeRegister.BIOME_GRAVEL_DESERT).getSpawnableList(EnumCreatureType.MONSTER).removeIf(entry -> EntityGhast.class.isAssignableFrom(entry.entityClass));
    }
}
