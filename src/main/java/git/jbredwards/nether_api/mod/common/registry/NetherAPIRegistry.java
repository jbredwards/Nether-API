/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.registry;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistryListener;
import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public class NetherAPIRegistry implements INetherAPIRegistry
{
    @Nonnull
    public static final NetherAPIRegistry NETHER = new NetherAPIRegistry(), THE_END = new NetherAPIRegistry();
    public NetherAPIRegistry() { REGISTRIES.add(this); }

    @Nonnull protected final List<BiomeManager.BiomeEntry> biomeEntries = new LinkedList<>();
    @Nonnull protected final List<INetherAPIStructureEntry> structureEntries = new LinkedList<>();

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getBiomeEntries() { return Collections.unmodifiableList(biomeEntries); }

    @Nonnull
    @Override
    public List<INetherAPIStructureEntry> getStructures() { return Collections.unmodifiableList(structureEntries); }

    @Override
    public void registerBiome(@Nonnull Biome biome, int weight) {
        //biome must be registered to forge registry
        if(biome.delegate.name() == null) throw new IllegalArgumentException("Biome must be registered!");
        if(weight < 1) return; //don't allow biomes with a weight less than 1

        removeBiome(biome); //no duplicate entries
        biomeEntries.add(new BiomeManager.BiomeEntry(biome, weight));

        //update biome if listener
        if(biome instanceof INetherAPIRegistryListener)
            ((INetherAPIRegistryListener)biome).onAddedToRegistry(this, OptionalInt.of(weight));
    }

    @Override
    public boolean removeBiome(@Nonnull Biome biome) {
        return biomeEntries.removeIf(entry -> {
            if(entry.biome == biome) {
                //update biome if listener
                if(biome instanceof INetherAPIRegistryListener)
                    ((INetherAPIRegistryListener)biome).onRemovedFromRegistry(this, OptionalInt.of(entry.itemWeight));

                return true;
            }

            return false;
        });
    }

    @Override
    public void registerStructure(@Nonnull INetherAPIStructureEntry structureEntry) {
        removeStructure(structureEntry.getCommandName());
        structureEntries.add(structureEntry);

        //update structure if listener
        if(structureEntry instanceof INetherAPIRegistryListener)
            ((INetherAPIRegistryListener)structureEntry).onAddedToRegistry(this, OptionalInt.empty());
    }

    @Override
    public void registerStructure(@Nonnull String commandName, @Nonnull Function<INetherAPIChunkGenerator, MapGenStructure> structureFactory) {
        registerStructure(new NetherAPIStructureEntry(commandName, structureFactory));
    }

    @Override
    public boolean removeStructure(@Nonnull String commandName) {
        return structureEntries.removeIf(entry -> {
            if(entry.getCommandName().equals(commandName)) {
                if(entry instanceof INetherAPIRegistryListener) ((INetherAPIRegistryListener)entry).onRemovedFromRegistry(this, OptionalInt.empty());
                return true;
            }

            return false;
        });
    }

    @Override
    public void clear() {
        for(final Iterator<BiomeManager.BiomeEntry> it = biomeEntries.iterator(); it.hasNext();) {
            final BiomeManager.BiomeEntry entry = it.next();
            if(entry.biome instanceof INetherAPIRegistryListener)
                ((INetherAPIRegistryListener)entry.biome).onRemovedFromRegistry(this, OptionalInt.of(entry.itemWeight));

            it.remove();
        }

        for(final Iterator<INetherAPIStructureEntry> it = structureEntries.iterator(); it.hasNext();) {
            final INetherAPIStructureEntry entry = it.next();
            if(entry instanceof INetherAPIRegistryListener)
                ((INetherAPIRegistryListener)entry).onRemovedFromRegistry(this, OptionalInt.empty());

            it.remove();
        }
    }
}
