package git.jbredwards.nether_api.mod.common.world.gen.layer;

import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
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
    public GenLayerNetherBiomes(long seed) {
        super(seed);
    }

    @Nonnull
    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        final int[] out = IntCache.getIntCache(areaWidth * areaHeight);
        final int totalWeight = WeightedRandom.getTotalWeight(NetherAPIRegistry.NETHER.getBiomes());

        for(int x = 0; x < areaWidth; x++) {
            for(int z = 0; z < areaHeight; z++) {
                initChunkSeed(x + areaX, z + areaZ);

                final Biome biome = WeightedRandom.getRandomItem(NetherAPIRegistry.NETHER.getBiomes(), nextInt(totalWeight)).biome;
                out[x + z * areaHeight] = Biome.getIdForBiome(biome);
            }
        }

        return out;
    }
}
