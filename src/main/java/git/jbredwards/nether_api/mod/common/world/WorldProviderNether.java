package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNether;
import net.minecraft.world.WorldProviderHell;

/**
 *
 * @author jbred
 *
 */
public class WorldProviderNether extends WorldProviderHell
{
    @Override
    public void init() {
        biomeProvider = new BiomeProviderNether(world.getWorldInfo());
        doesWaterVaporize = true;
        nether = true;
    }
}
