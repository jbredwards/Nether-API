/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.betternether;

import git.jbredwards.nether_api.api.biome.INetherAPIBiomeProvider;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistryListener;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHell;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import paulevs.betternether.biomes.NetherBiome;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * Wraps BetterNether pseudo-biomes with real ones.
 * @author jbred
 *
 */
public final class BiomeBetterNether extends BiomeHell implements INetherAPIBiomeProvider, INetherAPIRegistryListener
{
    @Nonnull
    static final Field SUBBIOMES_FIELD = ObfuscationReflectionHelper.findField(NetherBiome.class, "subbiomes");
    int cachedWeight = -1;

    @Nonnull
    public final NetherBiome netherBiome;
    public final int netherBiomeId;

    BiomeBetterNether(@Nonnull NetherBiome netherBiomeIn, int netherBiomeIdIn) {
        super(new BiomeProperties(netherBiomeIn.getName()).setTemperature(2).setRainfall(0).setRainDisabled());
        setRegistryName(NetherAPI.MODID, "betternether_" + netherBiomeIn.getClass().getSimpleName());

        netherBiome = netherBiomeIn;
        netherBiomeId = netherBiomeIdIn;
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getSubBiomes() {
        //this has no sub-biomes, return an empty list
        final List<NetherBiome> subBiomes = getSubNetherBiomes(netherBiome);
        if(subBiomes.isEmpty() || subBiomes.size() == 1 && subBiomes.get(0) == netherBiome) return Collections.emptyList();

        //return a list of valid sub-biomes
        return subBiomes.stream()
                .filter(netherBiomeIn -> BetterNetherHandler.getWeight(BetterNetherHandler.getBiomeFromLookup(netherBiomeIn)) > 0)
                .map(netherBiomeIn -> netherBiomeIn == netherBiome
                        ? new BiomeManager.BiomeEntry(this, 1000) //BetterNether sub-biomes have "chance in 1000" of spawning
                        : BetterNetherHandler.getBiomeFromLookup(netherBiomeIn).createBiomeEntry())
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getEdgeBiomes(int neighborBiomeId) {
        //this has no edge biome, return an empty list
        final NetherBiome edge = netherBiome.getEdge();
        if(edge == netherBiome) return Collections.emptyList();

        //if the neighboring biome is part of this one, don't create a separation between them lol
        final Biome neighborBiome = Biome.getBiomeForId(neighborBiomeId);
        if(neighborBiome == this) return Collections.emptyList();

        //if the neighboring biome is part of a sub-biome (that's associated with this biome), don't create a separation
        for(final BiomeManager.BiomeEntry entry : getSubBiomes()) if(entry.biome == neighborBiome) return Collections.emptyList();

        //BetterNether biomes can have a max of one edge biome
        return Collections.singletonList(BetterNetherHandler.getBiomeFromLookup(edge).createBiomeEntry());
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static List<NetherBiome> getSubNetherBiomes(@Nonnull NetherBiome netherBiome) {
        try { return (List<NetherBiome>)SUBBIOMES_FIELD.get(netherBiome); }
        //should never pass
        catch(IllegalAccessException e) { throw new RuntimeException(e); }
    }

    @Override
    public void onAddedToRegistry(@Nonnull INetherAPIRegistry registry, @Nonnull OptionalInt newWeight) {
        if(newWeight.isPresent()) cachedWeight = newWeight.getAsInt();
    }

    @Override
    public void onRemovedFromRegistry(@Nonnull INetherAPIRegistry registry, @Nonnull OptionalInt oldWeight) {
        cachedWeight = -1;
    }

    @Nonnull
    public BiomeManager.BiomeEntry createBiomeEntry() { return new BiomeManager.BiomeEntry(this, BetterNetherHandler.getWeight(this)); }
}
