package git.jbredwards.nether_api.mod.common.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public abstract class BiomeProviderNetherAPI extends BiomeProvider
{
    public BiomeProviderNetherAPI(@Nonnull WorldType worldType, long seed) {
        final GenLayer[] biomeGenerators = getModdedBiomeGenerators(worldType, seed, getBiomeGenerators(worldType, seed));

        genBiomes = biomeGenerators[0];
        biomeIndexLayer = biomeGenerators[1];

        biomesToSpawnIn = Collections.emptyList();
    }

    @Nonnull
    public abstract GenLayer[] getBiomeGenerators(@Nonnull WorldType worldType, long seed);

    @Nonnull
    @Override
    public abstract GenLayer[] getModdedBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull GenLayer[] original);

    @Nonnull
    @Override
    public Biome[] getBiomesForGeneration(@Nullable Biome[] listToReuse, int x, int z, int width, int height) {
        final int size = width * height;
        if(listToReuse == null || listToReuse.length < size) listToReuse = new Biome[size];

        //get biomes from GenLayer
        IntCache.resetIntCache();
        final int[] biomeIds = genBiomes.getInts(x, z, width, height);
        for(int i = 0; i < size; i++) {
            final int biomeId = biomeIds[i];
            listToReuse[i] = Objects.requireNonNull(Biome.getBiome(biomeId), () -> "Unmapped biome id: " + biomeId);
        }

        return listToReuse;
    }

    @Nonnull
    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int height, boolean cacheFlag) {
        final int size = width * height;
        if(listToReuse == null || listToReuse.length < size) listToReuse = new Biome[size];

        //get biomes from BiomeCache
        if(cacheFlag && width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0) {
            final Biome[] biomes = biomeCache.getCachedBiomes(x, z);
            System.arraycopy(biomes, 0, listToReuse, 0, size);
            return listToReuse;
        }

        //get biomes from GenLayer
        IntCache.resetIntCache();
        final int[] biomeIds = biomeIndexLayer.getInts(x, z, width, height);
        for(int i = 0; i < size; i++) {
            final int biomeId = biomeIds[i];
            listToReuse[i] = Objects.requireNonNull(Biome.getBiome(biomeId), () -> "Unmapped biome id: " + biomeId);
        }

        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, @Nonnull List<Biome> allowed) {
        return !allowed.isEmpty() && super.areBiomesViable(x, z, radius, allowed);
    }

    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, @Nonnull List<Biome> biomes, @Nonnull Random random) {
        return biomes.isEmpty() ? null : super.findBiomePosition(x, z, range, biomes, random);
    }
}
