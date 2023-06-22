package git.jbredwards.nether_api.mod.common.registry;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.structure.MapGenSpawningStructure;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @Nonnull final List<MapGenSpawningStructure> structureHandlers = new ArrayList<>();

    @Override
    public void registerBiome(@Nonnull Biome biome, int weight) {
        //biome must be registered to forge registry
        if(biome.delegate.name() == null) throw new IllegalArgumentException("Biome must be registered!");
        if(weight < 1) return; //don't allow biomes with a weight less than 1

        removeBiome(biome); //no duplicate entries
        biomes.add(new BiomeManager.BiomeEntry(biome, weight));
    }

    @Override
    public boolean removeBiome(@Nonnull Biome biome) {
        return biomes.removeIf(entry -> entry.biome == biome);
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getBiomes() { return Collections.unmodifiableList(biomes); }

    @Override
    public void registerStructure(@Nonnull MapGenSpawningStructure structureHandler) {
        removeStructure(structureHandler);
        structureHandlers.add(structureHandler);
    }

    @Override
    public boolean removeStructure(@Nonnull MapGenSpawningStructure structureHandler) { return structureHandlers.remove(structureHandler); }

    @Nonnull
    @Override
    public List<MapGenSpawningStructure> getStructureHandlers() { return Collections.unmodifiableList(structureHandlers); }

    @Override
    public void clear() {
        biomes.clear();
        structureHandlers.clear();
    }
}
