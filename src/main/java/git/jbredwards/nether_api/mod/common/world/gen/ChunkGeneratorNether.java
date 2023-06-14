package git.jbredwards.nether_api.mod.common.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorHell;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class ChunkGeneratorNether extends ChunkGeneratorHell
{
    public ChunkGeneratorNether(@Nonnull World worldIn, boolean generateStructures, long seed) {
        super(worldIn, generateStructures, seed);

    }
}
