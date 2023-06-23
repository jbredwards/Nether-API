package git.jbredwards.nether_api.mod.common.compat.netherex;

import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.biome.INetherBiomeProvider;
import git.jbredwards.nether_api.api.block.INetherCarvable;
import logictechcorp.libraryex.IModData;
import logictechcorp.libraryex.event.LibExEventFactory;
import logictechcorp.libraryex.world.biome.data.BiomeData;
import logictechcorp.netherex.NetherEx;
import logictechcorp.netherex.world.biome.BiomeNetherEx;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused") //used via asm
public abstract class AbstractNetherExBiome extends BiomeNetherEx implements INetherBiome, INetherBiomeProvider, INetherCarvable
{
    public AbstractNetherExBiome(@Nonnull IModData data, @Nonnull BiomeProperties properties, @Nonnull String name) {
        super(data, properties, name);
    }

    @Override
    public boolean canNetherCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z) {
        final BiomeData biomeData = NetherEx.BIOME_DATA_MANAGER.getBiomeData(this);
        return state == biomeData.getBiomeBlock(BiomeData.BlockType.SURFACE_BLOCK) || state == biomeData.getBiomeBlock(BiomeData.BlockType.SUBSURFACE_BLOCK);
    }

    @Nonnull
    @Override
    public List<BiomeManager.BiomeEntry> getSubBiomes() {
        return NetherEx.BIOME_DATA_MANAGER.getBiomeData(this).getSubBiomes().stream()
                .filter(BiomeData::isEnabled)
                .map(data -> new BiomeManager.BiomeEntry(data.getBiome(), data.getGenerationWeight()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void buildSurface(@Nonnull IChunkGenerator chunkGenerator, @Nonnull World world, @Nonnull Random rand, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer, double terrainNoise) {
        final int prevSeaLevel = world.getSeaLevel();
        world.setSeaLevel(31); //temporarily set sea level to match hardcoded lava height, otherwise NetherEx generation breaks
        NetherEx.BIOME_DATA_MANAGER.getBiomeData(this).generateTerrain(world, rand, primer, chunkX << 4 | x, chunkZ << 4 | z, terrainNoise);
        world.setSeaLevel(prevSeaLevel);
    }

    //heavily copied from ChunkGeneratorNetherEx to ensure NetherEx biomes generate as authentically as possible
    @Override
    public void decorate(@Nonnull IChunkGenerator chunkGenerator, @Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, boolean generateStructures) {
        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        
        ForgeEventFactory.onChunkPopulate(true, chunkGenerator, world, rand, chunkX, chunkZ, false);
        TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.CUSTOM);
        ForgeEventFactory.onChunkPopulate(false, chunkGenerator, world, rand, chunkX, chunkZ, false);
        LibExEventFactory.onPreDecorateBiome(world, rand, chunkPos);
        LibExEventFactory.onDecorateBiome(world, rand, chunkPos, pos, DecorateBiomeEvent.Decorate.EventType.CUSTOM);

        final BiomeData biomeData = NetherEx.BIOME_DATA_MANAGER.getBiomeData(this);
        if(biomeData != BiomeData.EMPTY && biomeData.useDefaultBiomeDecorations()) decorate(world, rand, pos);

        LibExEventFactory.onPostDecorateBiome(world, rand, chunkPos);
        LibExEventFactory.onPreOreGen(world, rand, pos);
        LibExEventFactory.onOreGen(world, rand, new WorldGenMinable(Blocks.AIR.getDefaultState(), 0, BlockMatcher.forBlock(Blocks.AIR)), pos, OreGenEvent.GenerateMinable.EventType.CUSTOM);
        LibExEventFactory.onPostOreGen(world, rand, pos);
    }
}
