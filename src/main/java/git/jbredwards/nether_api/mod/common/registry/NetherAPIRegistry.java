/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.registry;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistryListener;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.*;

/**
 *
 * @author jbred
 *
 */
public enum NetherAPIRegistry implements INetherAPIRegistry
{
    NETHER,
    THE_END;

    @Nonnull final List<BiomeManager.BiomeEntry> biomes = new ArrayList<>();
    @Nonnull final List<MapGenStructure> structureHandlers = new ArrayList<>();

    @Override
    public void registerBiome(@Nonnull Biome biome, int weight) {
        //biome must be registered to forge registry
        if(biome.delegate.name() == null) throw new IllegalArgumentException("Biome must be registered!");
        if(weight < 1) return; //don't allow biomes with a weight less than 1

        removeBiome(biome); //no duplicate entries
        biomes.add(new BiomeManager.BiomeEntry(biome, weight));

        //update biome if listener
        if(biome instanceof INetherAPIRegistryListener)
            ((INetherAPIRegistryListener)biome).onAddedToRegistry(this, OptionalInt.of(weight));
    }

    @Override
    public boolean removeBiome(@Nonnull Biome biome) {
        return biomes.removeIf(entry -> {
            if(entry.biome == biome) {
                //update biome if listener
                if(biome instanceof INetherAPIRegistryListener)
                    ((INetherAPIRegistryListener)biome).onRemovedFromRegistry(this, OptionalInt.of(entry.itemWeight));

                return true;
            }

            return false;
        });
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getBiomes() { return Collections.unmodifiableList(biomes); }

    @Override
    public void registerStructure(@Nonnull MapGenStructure structureHandler) {
        removeStructure(structureHandler);
        structureHandlers.add(structureHandler);

        //update structure if listener
        if(structureHandler instanceof INetherAPIRegistryListener)
            ((INetherAPIRegistryListener)structureHandler).onAddedToRegistry(this, OptionalInt.empty());
    }

    @Override
    public boolean removeStructure(@Nonnull MapGenStructure structureHandler) {
        return structureHandlers.removeIf(handler -> {
            if(handler == structureHandler) {
                //update structure if listener
                if(handler instanceof INetherAPIRegistryListener)
                    ((INetherAPIRegistryListener)handler).onRemovedFromRegistry(this, OptionalInt.empty());

                return true;
            }

            return false;
        });
    }

    @Nonnull
    @Override
    public List<MapGenStructure> getStructureHandlers() { return Collections.unmodifiableList(structureHandlers); }

    @Override
    public void clear() {
        for(final Iterator<BiomeManager.BiomeEntry> it = biomes.iterator(); it.hasNext();) {
            final BiomeManager.BiomeEntry entry = it.next();
            if(entry.biome instanceof INetherAPIRegistryListener)
                ((INetherAPIRegistryListener)entry.biome).onRemovedFromRegistry(this, OptionalInt.of(entry.itemWeight));

            it.remove();
        }

        for(final Iterator<MapGenStructure> it = structureHandlers.iterator(); it.hasNext();) {
            final MapGenStructure structureHandler = it.next();
            if(structureHandler instanceof INetherAPIRegistryListener)
                ((INetherAPIRegistryListener)structureHandler).onRemovedFromRegistry(this, OptionalInt.empty());

            it.remove();
        }
    }
}
