package git.jbredwards.nether_api.mod.common.world.gen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorEnd;

import javax.annotation.Nonnull;

/**
 * TODO, will use GenLayer to determine the placement of end islands and their biomes (one biome per end island)
 * @author jbred
 *
 */
public class ChunkGeneratorTheEnd extends ChunkGeneratorEnd
{
    public ChunkGeneratorTheEnd(@Nonnull World worldIn, boolean generateStructures, long seed, @Nonnull BlockPos spawnCoord) {
        super(worldIn, generateStructures, seed, spawnCoord);
    }
}
