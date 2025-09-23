/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.netherex;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import logictechcorp.libraryex.event.LibExEventFactory;
import logictechcorp.libraryex.world.biome.data.BiomeData;
import logictechcorp.netherex.NetherEx;
import logictechcorp.netherex.NetherExConfig;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.WorldEvent;

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
}
