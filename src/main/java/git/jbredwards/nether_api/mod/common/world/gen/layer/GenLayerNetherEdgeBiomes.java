/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen.layer;

import git.jbredwards.nether_api.api.biome.INetherBiomeProvider;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 *
 * @author jbred
 *
 */
public class GenLayerNetherEdgeBiomes extends GenLayer
{
    public GenLayerNetherEdgeBiomes(long seed, @Nonnull GenLayer parentIn) {
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
                final int index = x + z * areaHeight;
                out[index] = biomeId;

                //try to create edge-biomes if any adjacent biomes are different
                final int finalX = x;
                final int finalZ = z;
                for(final BooleanSupplier supplier : this.<BooleanSupplier>shuffle(
                () -> handleNeighborBiome(out, index, biomeIds[finalX + 1 + (finalZ + 1 - 1) * (areaHeight + 2)]),
                () -> handleNeighborBiome(out, index, biomeIds[finalX + 1 + 1 + (finalZ + 1) * (areaHeight + 2)]),
                () -> handleNeighborBiome(out, index, biomeIds[finalX + 1 - 1 + (finalZ + 1) * (areaHeight + 2)]),
                () -> handleNeighborBiome(out, index, biomeIds[finalX + 1 + (finalZ + 1 + 1) * (areaHeight + 2)]))) {
                    if(supplier.getAsBoolean()) break;
                }
            }
        }

        return out;
    }

    @Nonnull
    @SafeVarargs
    protected final <T> T[] shuffle(@Nonnull T... original) {
        for(int i = original.length - 1; i > 0; i--) {
            final T temp = original[i];
            final int rand = nextInt(i + 1);

            original[i] = original[rand];
            original[rand] = temp;
        }

        return original;
    }

    protected boolean handleNeighborBiome(@Nonnull int[] out, int index, int neighborId) {
        if(out[index] != neighborId) {
            final Biome biome = Biome.getBiomeForId(out[index]);
            if(biome instanceof INetherBiomeProvider) {
                final List<BiomeManager.BiomeEntry> edgeBiomes = ((INetherBiomeProvider)biome).getEdgeBiomes(neighborId);
                if(!edgeBiomes.isEmpty()) {
                    final int totalWeight = WeightedRandom.getTotalWeight(edgeBiomes);
                    final Biome edgeBiome = WeightedRandom.getRandomItem(edgeBiomes, nextInt(totalWeight)).biome;

                    out[index] = Biome.getIdForBiome(edgeBiome);
                    return true;
                }
            }
        }

        return false;
    }
}
