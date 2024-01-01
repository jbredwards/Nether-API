/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.biome;

import git.jbredwards.nether_api.api.event.NetherAPIBiomeSizeEvent;
import git.jbredwards.nether_api.api.event.NetherAPIInitBiomeGensEvent;
import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.gen.layer.GenLayerNetherBiomes;
import git.jbredwards.nether_api.mod.common.world.gen.layer.GenLayerNetherEdgeBiomes;
import git.jbredwards.nether_api.mod.common.world.gen.layer.GenLayerNetherSubBiomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class BiomeProviderNether extends BiomeProviderNetherAPI
{
    public BiomeProviderNether(@Nonnull World world) {
        super(world, NetherAPIRegistry.NETHER, NetherAPIRegistryEvent.Nether::new);
    }

    @Nonnull
    @Override
    public GenLayer[] getBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull INetherAPIRegistry registry) {
        //allow other mods to change the size of nether biomes
        final NetherAPIBiomeSizeEvent event = new NetherAPIBiomeSizeEvent.Nether(worldType, 3);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);

        //biome layer handlers
        final GenLayer biomeLayerBase = new GenLayerFuzzyZoom(10, new GenLayerNetherBiomes(20, registry));
        final GenLayer biomeLayerWithSub = GenLayerZoom.magnify(10, new GenLayerNetherSubBiomes(20, biomeLayerBase), event.biomeSize);
        final GenLayer biomeLayerWithEdge = new GenLayerNetherEdgeBiomes(20, biomeLayerWithSub);

        //returned biome layers
        final GenLayer biomeLayer = new GenLayerSmooth(10, new GenLayerSmooth(10, GenLayerZoom.magnify(10, biomeLayerWithEdge, 2)));
        final GenLayer indexLayer = new GenLayerVoronoiZoom(10, biomeLayer);
        indexLayer.initWorldGenSeed(seed); //this method also initiates all parent layers
        return new GenLayer[] {biomeLayer, indexLayer};
    }

    @Nonnull
    @Override
    public GenLayer[] getModdedBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull GenLayer[] original) {
        final NetherAPIInitBiomeGensEvent event = new NetherAPIInitBiomeGensEvent.Nether(worldType, seed, original);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.biomeGenerators;
    }
}
