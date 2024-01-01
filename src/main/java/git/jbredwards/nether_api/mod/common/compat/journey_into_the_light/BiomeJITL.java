/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.journey_into_the_light;

import git.jbredwards.nether_api.api.biome.INetherAPIBiomeProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.journey.dimension.nether.biomes.NetherBiome;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHell;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps Journey Into The Light mod pseudo-biomes with real ones.
 * @author jbred
 *
 */
public final class BiomeJITL extends BiomeHell implements INetherAPIBiomeProvider
{
    @Nonnull static final Field SUBBIOMES_FIELD = ObfuscationReflectionHelper.findField(NetherBiome.class, "subbiomes");
    @Nonnull public final Class<? extends NetherBiome> netherBiomeClass;

    @Nullable
    public NetherBiome netherBiome;
    BiomeJITL(@Nonnull Class<? extends NetherBiome> netherBiomeClassIn, @Nonnull String nameIn) {
        super(new BiomeProperties(nameIn).setTemperature(2).setRainfall(0).setRainDisabled());
        setRegistryName(NetherAPI.MODID, "journey_into_the_light_" + netherBiomeClassIn.getSimpleName());
        netherBiomeClass = netherBiomeClassIn;
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getEdgeBiomes(int neighborBiomeId) {
        if(netherBiome == null) throw new IllegalStateException("Attempted to generate unregistered JITL biome: {" + netherBiomeClass.getSimpleName() + '}');

        //this has no edge biome, return an empty list
        final NetherBiome edge = netherBiome.getEdge();
        if(edge == netherBiome) return Collections.emptyList();

        //if the neighboring biome is part of this one, don't create a separation between them lol
        final Biome neighborBiome = Biome.getBiomeForId(neighborBiomeId);
        if(neighborBiome == this) return Collections.emptyList();

        //if the neighboring biome is part of a sub-biome (that's associated with this biome), don't create a separation
        for(final BiomeManager.BiomeEntry entry : getSubBiomes()) if(entry.biome == neighborBiome) return Collections.emptyList();

        //Journey Into The Light biomes can have a max of one edge biome
        return Collections.singletonList(new BiomeManager.BiomeEntry(JITLHandler.getBiomeFromLookup(edge), 1));
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getSubBiomes() {
        if(netherBiome == null) throw new IllegalStateException("Attempted to generate unregistered JITL biome: {" + netherBiomeClass.getSimpleName() + '}');
        return Collections.unmodifiableList(getSubNetherBiomes(netherBiome).stream()
                .map(biome -> new BiomeManager.BiomeEntry(JITLHandler.getBiomeFromLookup(biome), 1))
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static List<NetherBiome> getSubNetherBiomes(@Nonnull NetherBiome netherBiome) {
        try { return (List<NetherBiome>)SUBBIOMES_FIELD.get(netherBiome); }
        catch(final IllegalAccessException e) { throw new RuntimeException(e); } // should never pass
    }
}
