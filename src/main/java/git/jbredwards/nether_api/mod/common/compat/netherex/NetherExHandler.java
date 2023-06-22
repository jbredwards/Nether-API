package git.jbredwards.nether_api.mod.common.compat.netherex;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import logictechcorp.libraryex.event.LibExEventFactory;
import logictechcorp.libraryex.world.biome.data.BiomeData;
import logictechcorp.netherex.NetherEx;
import logictechcorp.netherex.NetherExConfig;
import logictechcorp.netherex.init.NetherExBiomes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class NetherExHandler
{
    public static boolean doesXZShowFog() { return !NetherExConfig.client.visual.disableNetherFog; }

    public static boolean doesGravelGenerate() { return NetherExConfig.dimension.nether.generateGravel; }

    public static boolean doesSoulSandGenerate() { return NetherExConfig.dimension.nether.generateSoulSand; }

    public static void onChunkGenerate(@Nonnull Chunk chunk) { LibExEventFactory.onChunkGenerate(chunk); }

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        registerBiome(registry, NetherExBiomes.ARCTIC_ABYSS);
        registerBiome(registry, NetherExBiomes.FUNGI_FOREST);
        registerBiome(registry, NetherExBiomes.RUTHLESS_SANDS);
        registerBiome(registry, NetherExBiomes.TORRID_WASTELAND);
    }

    static void registerBiome(@Nonnull INetherAPIRegistry registry, @Nonnull Biome biome) {
        final BiomeData biomeData = NetherEx.BIOME_DATA_MANAGER.getBiomeData(biome);
        if(biomeData.isEnabled()) registry.registerBiome(biome, biomeData.getGenerationWeight());
    }

    @Nonnull
    public static List<Biome.SpawnListEntry> getSpawnableList(@Nonnull Biome biome, @Nonnull EnumCreatureType creatureType) {
        final List<Biome.SpawnListEntry> spawns = new ArrayList<>(biome.getSpawnableList(creatureType));
        final BiomeData biomeData = NetherEx.BIOME_DATA_MANAGER.getBiomeData(biome);

        if(biomeData != BiomeData.EMPTY) spawns.addAll(biomeData.getEntitySpawns(creatureType));
        return spawns;
    }
}
