package git.jbredwards.nether_api.mod.common.world.biome;

import git.jbredwards.nether_api.api.event.NetherAPIInitBiomeGensEvent;
import git.jbredwards.nether_api.mod.common.world.gen.layer.GenLayerNetherBiomes;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class BiomeProviderNether extends BiomeProviderNetherAPI
{
    public BiomeProviderNether(@Nonnull WorldType worldType, long seed) { super(worldType, seed); }

    @Nonnull
    @Override
    public GenLayer[] getBiomeGenerators(@Nonnull WorldType worldType, long seed) {
        GenLayer biomeLayer = new GenLayerNetherBiomes(2000);


        final GenLayer indexLayer = new GenLayerVoronoiZoom(10, biomeLayer);
        biomeLayer.initWorldGenSeed(seed);
        indexLayer.initWorldGenSeed(seed);

        return new GenLayer[] {biomeLayer, indexLayer};
    }

    @Nonnull
    @Override
    public GenLayer[] getModdedBiomeGenerators(@Nonnull WorldType worldType, long seed, @Nonnull GenLayer[] original) {
        final NetherAPIInitBiomeGensEvent event = new NetherAPIInitBiomeGensEvent.Nether(worldType, seed, original);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.biomeGenerators;
    }
}
