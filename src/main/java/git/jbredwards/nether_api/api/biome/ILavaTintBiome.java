package git.jbredwards.nether_api.api.biome;

/**
 * Biomes should implement this if they want lava to be tinted a different color.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ILavaTintBiome
{
    /**
     * @return the tint color of lava in this biome.
     */
    int getBiomeLavaColor();
}
