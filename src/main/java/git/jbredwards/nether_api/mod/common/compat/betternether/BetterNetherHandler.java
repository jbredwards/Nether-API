package git.jbredwards.nether_api.mod.common.compat.betternether;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import paulevs.betternether.biomes.BiomeRegister;
import paulevs.betternether.biomes.NetherBiome;
import paulevs.betternether.entities.EntityFirefly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class is very bad, mainly because I decided to add compatibility with both the legacy and forked versions of BetterNether
 * @author jbred
 *
 */
public final class BetterNetherHandler
{
    @Nonnull
    static final Map<NetherBiome, BiomeBetterNether> BIOME_LOOKUP = new HashMap<>();
    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        for(final BiomeBetterNether biome : BIOME_LOOKUP.values()) {
            //don't register the internal fallback biome for generation
            if(biome.netherBiome != BiomeRegister.BIOME_EMPTY_NETHER) {
                //don't register sub-biomes or edge biomes for individual generation
                if(biome.netherBiome == BiomeRegister.BIOME_MUSHROOM_FOREST_EDGE) break;
                else registry.registerBiome(biome, getWeight(biome.netherBiome));
            }
        }
    }

    @Nonnull
    public static BiomeBetterNether getBiomeFromLookup(@Nonnull NetherBiome netherBiome) {
        final BiomeBetterNether biome = BIOME_LOOKUP.get(netherBiome);
        if(biome != null) return biome;

        //should never pass
        throw new IllegalStateException("No Biome found for BetterNether: {" + netherBiome.getName() + '}');
    }

    public static int getWeight(@Nonnull NetherBiome netherBiome) {
        //will not be a WeightedRandom.Item if using legacy BetterNether
        return netherBiome instanceof WeightedRandom.Item ? netherBiome.itemWeight * 5 : 5;
    }

    @SubscribeEvent
    static void registerBiomes(@Nonnull RegistryEvent.Register<Biome> event) {
        final Consumer<NetherBiome> registerAction = netherBiome -> {
            final BiomeBetterNether biome = new BiomeBetterNether(netherBiome);
            event.getRegistry().register(biome);

            BIOME_LOOKUP.put(netherBiome, biome);
            BiomeDictionary.addTypes(biome, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY);
        };

        // ===========================
        // Legacy BetterNether version
        // ===========================
        try {
            final NetherBiome[] biomeRegistry = ObfuscationReflectionHelper.getPrivateValue(BiomeRegister.class, null, "BiomeRegistry");
            for(final NetherBiome netherBiome : biomeRegistry) {
                registerAction.accept(netherBiome);

                final NetherBiome edge = netherBiome.getEdge();
                if(edge != netherBiome && !BIOME_LOOKUP.containsKey(edge)) registerAction.accept(edge);

                final List<NetherBiome> subBiomes = BiomeBetterNether.getSubNetherBiomes(netherBiome);
                for(final NetherBiome subBiome : subBiomes) if(subBiome != netherBiome && !BIOME_LOOKUP.containsKey(subBiome)) registerAction.accept(subBiome);
            }
        }

        // ===========================
        // Forked BetterNether version
        // ===========================
        catch(Exception e) { BiomeRegister.BIOME_REGISTRY.values().forEach(registerAction); }
    }

    //exists because this mod adds BetterNether biomes as real biomes
    public static void resetFireflyBiomes() {
        Biomes.HELL.getSpawnableList(EnumCreatureType.AMBIENT).removeIf(entry -> entry.entityClass == EntityFirefly.class);
        EntityRegistry.addSpawn(EntityFirefly.class, 100, 5, 10, EnumCreatureType.AMBIENT,
                getBiomeFromLookup(BiomeRegister.BIOME_GRASSLANDS), getBiomeFromLookup(BiomeRegister.BIOME_NETHER_JUNGLE)
        );
    }
}
