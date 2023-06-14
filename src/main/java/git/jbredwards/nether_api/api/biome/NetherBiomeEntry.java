package git.jbredwards.nether_api.api.biome;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class NetherBiomeEntry extends BiomeManager.BiomeEntry
{
    @Nullable
    public final INetherBiome netherBiome;
    public NetherBiomeEntry(@Nonnull Biome biome, int weight) {
        super(biome, weight);
        netherBiome = biome instanceof INetherBiome ? (INetherBiome)biome : null;
    }


}
