package git.jbredwards.nether_api.api.registry;

import git.jbredwards.nether_api.api.structure.MapGenSpawningStructure;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles the generation of certain things, including End and Nether biomes.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherAPIRegistry
{
    /**
     * @return an immutable list containing all the Biomes in the generation list.
     */
    List<BiomeManager.BiomeEntry> getBiomes();

    /**
     * @return an immutable list containing all the structure handlers in the generation list.
     */
    List<MapGenSpawningStructure> getStructures();

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
     * Adds the structure handler to the generation list. Structures do not have to be registered in order to generate,
     * but do if you want them to be able to spawn mobs within its area (like nether fortresses do).
     */
    void registerStructure(@Nonnull MapGenSpawningStructure structure);

    /**
     * Removes the structure handler from the generation list if present.
     * @return true if the structure handler was removed from the generation list.
     */
    boolean removeStructure(@Nonnull MapGenSpawningStructure structure);

    /**
     * Removes all the biomes and structures from the Nether generation list.
     */
    void clear();
}
