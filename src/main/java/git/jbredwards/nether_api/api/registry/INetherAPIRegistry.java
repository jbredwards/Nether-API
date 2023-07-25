/*
 * Copyright (c) 2023. jbredwards
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
     * @since 1.2.1
     */
    @Nonnull
    List<INetherAPIRegistry> REGISTRIES = new LinkedList<>();

    /**
     * @return an immutable list containing all the Biomes in the generation list.
     * @since 1.2.1
     */
    List<BiomeManager.BiomeEntry> getBiomeEntries();

    /**
     * Adds the Biome to the generation list, with the provided weight.
     */
    void registerBiome(@Nonnull Biome biome, int weight);

    /**
     * Removes the Biome from the generation list if present.
     * @return true if the biome was removed from the generation list.
     */
    boolean removeBiome(@Nonnull Biome biome);

    /**
     * Removes all the biomes and structures from the Nether generation list.
     */
    void clear();

    /**
     * @return an immutable list containing all the structure handlers in the generation list.
     * @since 1.2.1
     */
    List<INetherAPIStructureEntry> getStructures();

    /**
     * Adds the structure handler to the generation list. Structures do not have to be registered in order to generate,
     * but do if you want them to be able to spawn mobs within its area (like nether fortresses do), and if you want /locate to work with it.
     *
     * @since 1.2.1
     */
    void registerStructure(@Nonnull INetherAPIStructureEntry structureEntry);

    /**
     * Adds the structure handler to the generation list. Structures do not have to be registered in order to generate,
     * but do if you want them to be able to spawn mobs within its area (like nether fortresses do), and if you want /locate to work with it.
     *
     * @param commandName the name of the structure used by the /locate command (should match the result of calling the structure's {@link MapGenStructure#getStructureName()} method).
     * @param structureFactory responsible for initializing the structure during the construction of the registry's respective {@link INetherAPIChunkGenerator}.
     *
     * @since 1.2.1
     */
    void registerStructure(@Nonnull String commandName, @Nonnull Function<INetherAPIChunkGenerator, MapGenStructure> structureFactory);

    /**
     * Removes the structure handler from the generation list if present.
     * @return true if the structure handler was removed from the generation list.
     *
     * @since 1.2.1
     */
    boolean removeStructure(@Nonnull String commandName);

    /**
     * @return true if this registry currently has no biomes or structures.
     * @since 1.2.1
     */
    default boolean isEmpty() { return getBiomeEntries().isEmpty() && getStructures().isEmpty(); }
}
