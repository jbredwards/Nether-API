/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.registry;

import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Handles the registration of biomes and structures for dimensional generation.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherAPIRegistry
{
    /**
     * Contains all {@link INetherAPIRegistry} instances. This is used by the /locate command to auto-complete structure names.
     * @since 1.3.0
     */
    @Nonnull
    List<INetherAPIRegistry> REGISTRIES = new LinkedList<>();

    /**
     * Removes all the biomes and structures from the generation list.
     * @since 1.0.0
     */
    void clear();

    /**
     * @return true if this registry currently has no biomes or structures.
     * @since 1.3.0
     */
    default boolean isEmpty() {
        return getBiomeEntries().isEmpty() && getStructures().isEmpty();
    }

    /**
     * @return an immutable list containing all the Biomes in the generation list.
     * @since 1.3.0
     */
    @Nonnull
    List<BiomeManager.BiomeEntry> getBiomeEntries();

    /**
     * Adds the Biome to the generation list, with the provided weight.
     * @since 1.0.0
     */
    void registerBiome(@Nonnull final Biome biome, final int weight);

    /**
     * Adds the BiomeEntry to the generation list.
     * @since 1.4.0
     */
    default void registerBiome(@Nonnull final BiomeManager.BiomeEntry biomeEntry) {
        registerBiome(biomeEntry.biome, biomeEntry.itemWeight);
    }

    /**
     * Removes the Biome from the generation list if present.
     * @return true if the biome was removed from the generation list.
     * @since 1.0.0
     */
    boolean removeBiome(@Nonnull final Biome biome);

    /**
     * @return an immutable list containing all the structure handlers in the generation list.
     * @since 1.3.0
     */
    @Nonnull
    List<INetherAPIStructureEntry> getStructures();

    /**
     * Adds the structure handler to the generation list. Structures do not have to be registered in order to generate,
     * but do if you want them to be able to spawn mobs within its area (like nether fortresses do), and if you want /locate to work with it.
     *
     * @since 1.3.0
     */
    void registerStructure(@Nonnull final INetherAPIStructureEntry structureEntry);

    /**
     * Adds the structure handler to the generation list. Structures do not have to be registered in order to generate,
     * but do if you want them to be able to spawn mobs within its area (like nether fortresses do), and if you want /locate to work with it.
     *
     * @param commandName the name of the structure used by the /locate command (should match the result of calling the structure's {@link MapGenStructure#getStructureName()} method).
     * @param structureFactory responsible for initializing the structure during the construction of the registry's respective {@link INetherAPIChunkGenerator}.
     *
     * @since 1.3.0
     */
    void registerStructure(@Nonnull final String commandName, @Nonnull final Function<INetherAPIChunkGenerator, MapGenStructure> structureFactory);

    /**
     * Removes the structure handler from the generation list if present.
     * @return true if the structure handler was removed from the generation list.
     *
     * @since 1.3.0
     */
    boolean removeStructure(@Nonnull final String commandName);
}
