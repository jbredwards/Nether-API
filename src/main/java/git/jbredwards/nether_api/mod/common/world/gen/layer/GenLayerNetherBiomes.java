/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen.layer;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class GenLayerNetherBiomes extends GenLayer
{
    @Nonnull
    protected final INetherAPIRegistry registry;
    public GenLayerNetherBiomes(long seed, @Nonnull INetherAPIRegistry registryIn) {
        super(seed);
        registry = registryIn;
    }

    @Nonnull
    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        final int[] out = IntCache.getIntCache(areaWidth * areaHeight);
        final int totalWeight = WeightedRandom.getTotalWeight(registry.getBiomeEntries());

        for(int x = 0; x < areaWidth; x++) {
            for(int z = 0; z < areaHeight; z++) {
                initChunkSeed(x + areaX, z + areaZ);

                final Biome biome = WeightedRandom.getRandomItem(registry.getBiomeEntries(), nextInt(totalWeight)).biome;
                out[x + z * areaHeight] = Biome.getIdForBiome(biome);
            }
        }

        return out;
    }
}
