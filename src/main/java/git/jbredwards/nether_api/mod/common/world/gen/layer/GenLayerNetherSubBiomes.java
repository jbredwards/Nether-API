/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen.layer;

import git.jbredwards.nether_api.api.biome.INetherAPIBiomeProvider;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class GenLayerNetherSubBiomes extends GenLayer
{
    public GenLayerNetherSubBiomes(long seed, @Nonnull GenLayer parentIn) {
        super(seed);
        parent = parentIn;
    }

    @Nonnull
    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        final int[] out = IntCache.getIntCache(areaWidth * areaHeight);
        final int[] biomeIds = parent.getInts(areaX - 1, areaZ - 1, areaWidth + 2, areaHeight + 2);

        for(int x = 0; x < areaWidth; x++) {
            for(int z = 0; z < areaHeight; z++) {
                initChunkSeed(areaX + x, areaZ + z);

                final int biomeId = biomeIds[x + 1 + (z + 1) * (areaHeight + 2)];
                out[x + z * areaHeight] = biomeId;

                //create sub-biome if all adjacent biomes are the same
                if(biomeId == biomeIds[x + 1 + (z + 1 - 1) * (areaHeight + 2)]
                && biomeId == biomeIds[x + 1 + 1 + (z + 1) * (areaHeight + 2)]
                && biomeId == biomeIds[x + 1 - 1 + (z + 1) * (areaHeight + 2)]
                && biomeId == biomeIds[x + 1 + (z + 1 + 1) * (areaHeight + 2)]) {
                    final Biome biome = Biome.getBiomeForId(biomeId);
                    if(biome instanceof INetherAPIBiomeProvider) {
                        final List<BiomeManager.BiomeEntry> subBiomes = ((INetherAPIBiomeProvider)biome).getSubBiomes();
                        if(!subBiomes.isEmpty()) {
                            final int totalWeight = WeightedRandom.getTotalWeight(subBiomes);
                            final Biome subBiome = WeightedRandom.getRandomItem(subBiomes, nextInt(totalWeight)).biome;

                            out[x + z * areaHeight] = Biome.getIdForBiome(subBiome);
                        }
                    }
                }
            }
        }

        return out;
    }
}
