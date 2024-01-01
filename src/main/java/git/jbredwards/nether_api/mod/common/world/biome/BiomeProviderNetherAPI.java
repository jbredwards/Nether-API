/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.biome;

import com.google.common.collect.ImmutableList;
import git.jbredwards.nether_api.api.biome.INoSpawnBiome;
import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

/**
 *
 * @author jbred
 *
 */
public abstract class BiomeProviderNetherAPI extends BiomeProvider
{
    @SuppressWarnings("UnstableApiUsage")
    protected BiomeProviderNetherAPI(@Nonnull World world, @Nonnull INetherAPIRegistry registry, @Nonnull BiFunction<INetherAPIRegistry, World, NetherAPIRegistryEvent> eventSupplier) {
        if(registry.isEmpty()) MinecraftForge.EVENT_BUS.post(eventSupplier.apply(registry, world));
        if(registry.getBiomeEntries().isEmpty()) {
            final String id = world.provider.getDimensionType().getName();
            throw new IllegalStateException("Dimension with id: \"" + id + "\" has no biomes, try adjusting your config settings!");
        }

        final GenLayer[] biomeGenerators =
                getModdedBiomeGenerators(world.getWorldType(), world.getSeed(),
                getBiomeGenerators(world.getWorldType(), world.getSeed(), registry));

        genBiomes = biomeGenerators[0];
        biomeIndexLayer = biomeGenerators[1];
        biomesToSpawnIn = registry.getBiomeEntries().stream()
                .map(entry -> entry.biome)
                .filter(biome -> !(biome instanceof INoSpawnBiome))
                .collect(ImmutableList.toImmutableList());
    }

    @Nonnull
    public abstract GenLayer[] getBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull INetherAPIRegistry registry);

    @Nonnull
    @Override
    public abstract GenLayer[] getModdedBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull GenLayer[] original);

    @Nonnull
    @Override
    public Biome[] getBiomesForGeneration(@Nullable Biome[] listToReuse, int x, int z, int width, int height) {
        IntCache.resetIntCache();
        final int size = width * height;
        if(listToReuse == null || listToReuse.length < size) listToReuse = new Biome[size];

        //get biomes from GenLayer
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
        IntCache.resetIntCache();
        final int size = width * height;
        if(listToReuse == null || listToReuse.length < size) listToReuse = new Biome[size];

        //get biomes from BiomeCache
        if(cacheFlag && width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0) {
            final Biome[] biomes = biomeCache.getCachedBiomes(x, z);
            System.arraycopy(biomes, 0, listToReuse, 0, size);
            return listToReuse;
        }

        //get biomes from GenLayer
        final int[] biomeIds = biomeIndexLayer.getInts(x, z, width, height);
        for(int i = 0; i < size; i++) {
            final int biomeId = biomeIds[i];
            listToReuse[i] = Objects.requireNonNull(Biome.getBiome(biomeId), () -> "Unmapped biome id: " + biomeId);
        }

        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, @Nonnull List<Biome> allowed) {
        return !allowed.isEmpty() && super.areBiomesViable(x, z, radius << 2, allowed);
    }

    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, @Nonnull List<Biome> biomes, @Nonnull Random random) {
        return biomes.isEmpty() ? null : super.findBiomePosition(x, z, range << 2, biomes, random);
    }
}
