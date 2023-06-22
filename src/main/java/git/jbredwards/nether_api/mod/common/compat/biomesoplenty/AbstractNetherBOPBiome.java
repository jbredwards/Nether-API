package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.common.biome.nether.BOPHellBiome;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused") //used via asm
public abstract class AbstractNetherBOPBiome extends BOPHellBiome implements INetherBiome
{
    public AbstractNetherBOPBiome(@Nonnull String idName, @Nonnull PropsBuilder defaultBuilder) {
        super(idName, defaultBuilder);
    }

    @Nonnull
    @Override
    public IBlockState getTopBlock() { return topBlock; }

    @Nonnull
    @Override
    public IBlockState getFillerBlock() { return fillerBlock; }

    @Nonnull
    @Override
    public IBlockState getLiquidBlock() { return Blocks.LAVA.getDefaultState(); }

    @Override
    public boolean canNetherCarveThrough(@Nonnull IBlockState state, @Nonnull ChunkPrimer primer, int x, int y, int z) {
        return getTopBlock() == state || getFillerBlock() == state || wallBlock == state || roofTopBlock == state || roofFillerBlock == state;
    }

    @Override
    public void buildSurface(@Nonnull IChunkGenerator chunkGenerator, @Nonnull World world, @Nonnull Random rand, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer) {
        NetherGenerationUtils.buildSurfaceAndSoulSandGravel(world, rand, primer, x, z, soulSandNoise, gravelNoise, depthBuffer, Blocks.NETHERRACK.getDefaultState(), Blocks.NETHERRACK.getDefaultState(), Blocks.NETHERRACK.getDefaultState(), getLiquidBlock());
        genTerrainBlocks(world, rand, primer, chunkX << 4 | x, chunkZ << 4 | z, 0); //this isn't the overworld, so don't provide an overworld noise value (isn't used by BOP for nether generation anyway)
    }

    //heavily copied from ChunkGeneratorHellBOP to ensure BOP biomes generate as authentically as possible
    @Override
    public void decorate(@Nonnull IChunkGenerator chunkGenerator, @Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, boolean generateStructures) {
        final boolean prevLogging = ForgeModContainer.logCascadingWorldGeneration;
        ForgeModContainer.logCascadingWorldGeneration = false;
        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;

        ForgeEventFactory.onChunkPopulate(true, chunkGenerator, world, rand, chunkX, chunkZ, false);
        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
            for (int k = 0; k < 8; ++k)
                new WorldGenHellLava(Blocks.FLOWING_LAVA, false).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));

        // don't do this to prevent double-ups
        //MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, pos));

        // note: this was moved earlier to be more similar to overworld biome decoration, however
        // it's possible that this may cause issues with other mods
        decorate(world, rand, pos);

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.FIRE))
            for (int i1 = 0; i1 < rand.nextInt(rand.nextInt(10) + 1) + 1; ++i1) {
                new WorldGenFire().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
            }

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.GLOWSTONE)) {
            for (int j1 = 0; j1 < rand.nextInt(rand.nextInt(10) + 1); ++j1) {
                new WorldGenGlowStone1().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(120) + 4, rand.nextInt(16) + 8));
            }

            for (int k1 = 0; k1 < 10; ++k1) {
                new WorldGenGlowStone2().generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }
        }//Forge: End doGlowstone

        ForgeEventFactory.onChunkPopulate(false, chunkGenerator, world, rand, chunkX, chunkZ, false);

        if (TerrainGen.decorate(world, rand, pos, DecorateBiomeEvent.Decorate.EventType.SHROOM)) {
            if (rand.nextBoolean()) {
                new WorldGenBush(Blocks.BROWN_MUSHROOM).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }

            if (rand.nextBoolean()) {
                new WorldGenBush(Blocks.RED_MUSHROOM).generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(128), rand.nextInt(16) + 8));
            }
        }

        final WorldGenerator quartzGen = new WorldGenMinable(Blocks.QUARTZ_ORE.getDefaultState(), 14, BlockMatcher.forBlock(Blocks.NETHERRACK));
        if (TerrainGen.generateOre(world, rand, quartzGen, pos, OreGenEvent.GenerateMinable.EventType.QUARTZ))
            for (int l1 = 0; l1 < 16; ++l1) {
                quartzGen.generate(world, rand, pos.add(rand.nextInt(16), rand.nextInt(108) + 10, rand.nextInt(16)));
            }

        int i2 = world.getSeaLevel() / 2 + 1;

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
            for (int l = 0; l < 4; ++l) {
                new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33, BlockMatcher.forBlock(Blocks.NETHERRACK))
                        .generate(world, rand, pos.add(rand.nextInt(16), i2 - 5 + rand.nextInt(10), rand.nextInt(16)));
            }

        if (TerrainGen.populate(chunkGenerator, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
            for (int j2 = 0; j2 < 16; ++j2) {
                new WorldGenHellLava(Blocks.FLOWING_LAVA, true).generate(world, rand, pos.add(rand.nextInt(16), rand.nextInt(108) + 10, rand.nextInt(16)));
            }

        // this should already be called during biome decoration (Vanilla doesn't usually call this for the Nether
        // though, since the decoration method is empty)
        //MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, pos));

        ForgeModContainer.logCascadingWorldGeneration = prevLogging;
    }
}
