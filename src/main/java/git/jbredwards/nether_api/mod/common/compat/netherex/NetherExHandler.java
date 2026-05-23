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

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import logictechcorp.libraryex.event.LibExEventFactory;
import logictechcorp.libraryex.world.biome.data.BiomeData;
import logictechcorp.netherex.NetherEx;
import logictechcorp.netherex.NetherExConfig;
import logictechcorp.netherex.init.NetherExBlocks;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.Item;
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

    public static void init() {
        // Add elder mushroom plants to creative tab.
        Item.getItemFromBlock(NetherExBlocks.BROWN_ELDER_MUSHROOM).setCreativeTab(NetherEx.instance.getCreativeTab());
        Item.getItemFromBlock(NetherExBlocks.RED_ELDER_MUSHROOM).setCreativeTab(NetherEx.instance.getCreativeTab());
    }
}
