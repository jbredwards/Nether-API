package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class WorldProviderTheEnd extends WorldProviderEnd
{
    @Override
    public void init() {
        biomeProvider = new BiomeProviderTheEnd(world.getWorldType(), world.getSeed());
        if(world instanceof WorldServer) dragonFightManager = new DragonFightManager((WorldServer)world, world.getWorldInfo().getDimensionData(getDimension()).getCompoundTag("DragonFight"));
    }

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorTheEnd(world, world.getWorldInfo().isMapFeaturesEnabled(), world.getSeed(), getSpawnCoordinate());
    }


}
