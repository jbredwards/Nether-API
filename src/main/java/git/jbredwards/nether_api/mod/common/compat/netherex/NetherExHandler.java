/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.mod.common.compat.netherex;

import git.jbredwards.nether_api.api.event.BiomeStructureEvent;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import logictechcorp.libraryex.event.LibExEventFactory;
import logictechcorp.libraryex.world.biome.data.BiomeData;
import logictechcorp.netherex.NetherEx;
import logictechcorp.netherex.NetherExConfig;
import logictechcorp.netherex.entity.monster.EntityFrost;
import logictechcorp.netherex.init.NetherExBiomes;
import logictechcorp.netherex.init.NetherExBlocks;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class NetherExHandler
{
    public static boolean doesXZShowFog() { return !NetherExConfig.client.visual.disableNetherFog; }

    public static boolean doesGravelGenerate() { return NetherExConfig.dimension.nether.generateGravel; }

    public static boolean doesSoulSandGenerate() { return NetherExConfig.dimension.nether.generateSoulSand; }

    public static void onChunkGenerate(@Nonnull final Chunk chunk) { LibExEventFactory.onChunkGenerate(chunk); }

    public static void registerBiomes(@Nonnull final INetherAPIRegistry registry, @Nonnull final World world) {
        // Read NetherEx data, which is no longer handled on world loading.
        NetherEx.BIOME_DATA_MANAGER.onWorldUnload(new WorldEvent.Unload(world));
        NetherEx.BIOME_DATA_MANAGER.onWorldLoad(new WorldEvent.Load(world));
        // Copy NetherEx biome entries to Nether API registry.
        NetherEx.BIOME_DATA_MANAGER.getCurrentBiomeEntries().values().forEach(registry::registerBiome);
    }

    @Nonnull
    public static List<Biome.SpawnListEntry> getSpawnableList(@Nonnull final Biome biome, @Nonnull final EnumCreatureType creatureType) {
        @Nonnull final List<Biome.SpawnListEntry> spawns = new ArrayList<>(biome.getSpawnableList(creatureType));
        @Nonnull final BiomeData biomeData = NetherEx.BIOME_DATA_MANAGER.getBiomeData(biome);

        if(biomeData != BiomeData.EMPTY) spawns.addAll(biomeData.getEntitySpawns(creatureType));
        return spawns;
    }

    public static void init() {
        // Add elder mushroom plants to creative tab.
        Item.getItemFromBlock(NetherExBlocks.BROWN_ELDER_MUSHROOM).setCreativeTab(NetherEx.instance.getCreativeTab());
        Item.getItemFromBlock(NetherExBlocks.RED_ELDER_MUSHROOM).setCreativeTab(NetherEx.instance.getCreativeTab());
        // Use NetherEx nether bricks for fortresses.
        if(NetherAPIConfig.NetherEx.fortressBiomeVariants) MinecraftForge.TERRAIN_GEN_BUS.register(NetherExHandler.class);
    }

    // ------------------------------
    // Nether Fortress Biome Variants
    // ------------------------------

    @SubscribeEvent
    static void useSuitableNetherBricks(@Nonnull final BiomeStructureEvent.GetBlockID event) {
        if(!event.structureName.equals("Fortress"));

        else if(event.getBiome() == NetherExBiomes.ARCTIC_ABYSS) {
            if(event.original.getBlock() == Blocks.NETHER_BRICK) event.setReplacement(NetherExBlocks.ICY_NETHER_BRICK.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_FENCE) event.setReplacement(NetherExBlocks.ICY_NETHER_BRICK_FENCE.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_STAIRS) event.setReplacement(NetherExBlocks.ICY_NETHER_BRICK_STAIRS.getDefaultState());
        }

        else if(event.getBiome() == NetherExBiomes.FUNGI_FOREST) {
            if(event.original.getBlock() == Blocks.NETHER_BRICK) event.setReplacement(NetherExBlocks.LIVELY_NETHER_BRICK.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_FENCE) event.setReplacement(NetherExBlocks.LIVELY_NETHER_BRICK_FENCE.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_STAIRS) event.setReplacement(NetherExBlocks.LIVELY_NETHER_BRICK_STAIRS.getDefaultState());
        }

        else if(event.getBiome() == NetherExBiomes.RUTHLESS_SANDS) {
            if(event.original.getBlock() == Blocks.NETHER_BRICK) event.setReplacement(NetherExBlocks.GLOOMY_NETHER_BRICK.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_FENCE) event.setReplacement(NetherExBlocks.GLOOMY_NETHER_BRICK_FENCE.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_STAIRS) event.setReplacement(NetherExBlocks.GLOOMY_NETHER_BRICK_STAIRS.getDefaultState());
        }

        else if(event.getBiome() == NetherExBiomes.TORRID_WASTELAND) {
            if(event.original.getBlock() == Blocks.NETHER_BRICK) event.setReplacement(NetherExBlocks.FIERY_NETHER_BRICK.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_FENCE) event.setReplacement(NetherExBlocks.FIERY_NETHER_BRICK_FENCE.getDefaultState());
            if(event.original.getBlock() == Blocks.NETHER_BRICK_STAIRS) event.setReplacement(NetherExBlocks.FIERY_NETHER_BRICK_STAIRS.getDefaultState());
        }
    }

    @SubscribeEvent
    static void useFrostBlaze(@Nonnull final BiomeStructureEvent.PrepareSpawnerLogic event) {
        if(NetherAPIConfig.NetherEx.fortressSpawnsFrost && event.structureName.equals("Fortress")
        && event.getBiome() == NetherExBiomes.ARCTIC_ABYSS && event.original.toString().equals("minecraft:blaze")) {
            event.spawnerLogic.setEntityId(NetherEx.getResource("frost"));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void useFrostBlaze(@Nonnull final BiomeStructureEvent.PrepareSpawnList event) {
        if(NetherAPIConfig.NetherEx.fortressSpawnsFrost && event.structureName.equals("Fortress") && event.getBiome() == NetherExBiomes.ARCTIC_ABYSS) {
            for(@Nonnull final Biome.SpawnListEntry entry : event.spawnList) if(entry.entityClass == EntityBlaze.class) entry.entityClass = EntityFrost.class;
        }
    }
}
