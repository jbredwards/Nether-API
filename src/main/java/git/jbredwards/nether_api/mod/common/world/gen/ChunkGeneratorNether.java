package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.structure.MapGenSpawningStructure;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class ChunkGeneratorNether extends ChunkGeneratorHell
{
    @Nonnull
    protected Biome[] biomesForGeneration = new Biome[0];
    public ChunkGeneratorNether(@Nonnull World worldIn, boolean generateStructures, long seed) {
        super(worldIn, generateStructures, seed);
    }

    @Override
    public void prepareHeights(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) {
        //TODO: add custom nether heights handler
        super.prepareHeights(chunkX, chunkZ, primer);
    }

    @Override
    public void buildSurfaces(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) {
        if(!ForgeEventFactory.onReplaceBiomeBlocks(this, chunkX, chunkZ, primer, world)) return;
        final int originX = chunkX << 4;
        final int originZ = chunkZ << 4;

        slowsandNoise = slowsandGravelNoiseGen.generateNoiseOctaves(slowsandNoise, originX, originZ, 0, 16, 16, 1, 0.03125, 0.03125, 1);
        gravelNoise = slowsandGravelNoiseGen.generateNoiseOctaves(gravelNoise, originX, 109, originZ, 16, 1, 16, 0.03125, 1, 0.03125);
        depthBuffer = netherrackExculsivityNoiseGen.generateNoiseOctaves(depthBuffer, originX, originZ, 0, 16, 16, 1, 0.0625, 0.0625, 0.0625);

        for(int posX = 0; posX < 16; posX++) {
            for(int posZ = 0; posZ < 16; posZ++) {
                //generate the bedrock
                for(int posY = 4; posY >= 0; posY--) {
                    if(posY <= rand.nextInt(5)) primer.setBlockState(posZ, posY, posX, BEDROCK);
                    if(posY >= 4 - rand.nextInt(5)) primer.setBlockState(posZ, posY + 123, posX, BEDROCK);
                }

                //replace netherrack top and filler blocks, and generate random soul sand & gravel
                final Biome biome = biomesForGeneration[(posZ << 4) | posX];
                if(biome instanceof INetherBiome) ((INetherBiome)biome).buildSurface(world, rand, chunkX, chunkZ, primer, posX, posZ, slowsandNoise, gravelNoise, depthBuffer);
                else NetherGenerationUtils.buildSurfaceAndSoulSandGravel(world, rand, primer, posX, posZ, slowsandNoise, gravelNoise, depthBuffer, NETHERRACK, biome.topBlock, biome.fillerBlock);
            }
        }
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int x, int z) {
        rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        biomesForGeneration = world.getBiomeProvider().getBiomes(null, x << 4, z << 4, 16, 16);

        final ChunkPrimer primer = new ChunkPrimer();
        prepareHeights(x, z, primer);
        buildSurfaces(x, z, primer);

        genNetherCaves.generate(world, x, z, primer);
        if(generateStructures) {
            final Biome biome = biomesForGeneration[255];
            if(!(biome instanceof INetherBiome) || ((INetherBiome)biome).canGenerateNetherFortress()) genNetherBridge.generate(world, x, z, primer);
            NetherAPIRegistry.NETHER.getStructures().forEach(structure -> structure.generate(world, x, z, primer));
        }

        final Chunk chunk = new Chunk(world, primer, x, z);
        byte[] biomeArray = chunk.getBiomeArray();
        for(int i = 0; i < biomeArray.length; ++i) biomeArray[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);

        chunk.resetRelightChecks();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        //ensure forge's fix is active when this runs, otherwise the console gets spammed
        //if you're using this mod, you don't care about the nether being identical to vanilla 1.12 lol
        final boolean prevFixVanillaCascading = ForgeModContainer.fixVanillaCascading;
        ForgeModContainer.fixVanillaCascading = true;

        final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        final Biome biome = world.getBiome(pos.add(16, 0, 16));

        if(biome instanceof INetherBiome) ((INetherBiome)biome).decorate(this, world, rand, pos, generateStructures);
        else NetherGenerationUtils.generateVanillaNetherFeatures(world, rand, pos, generateStructures, true);

        NetherAPIRegistry.NETHER.getStructures().forEach(structure -> structure.generateStructure(world, rand, new ChunkPos(chunkX, chunkZ)));

        //restore old vanilla cascading fix settings
        ForgeModContainer.fixVanillaCascading = prevFixVanillaCascading;
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
        if(generateStructures) {
            //vanilla
            if(creatureType == EnumCreatureType.MONSTER && genNetherBridge != null
            && (genNetherBridge.isInsideStructure(pos) || genNetherBridge.isPositionInStructure(world, pos) && world.getBlockState(pos.down()).getBlock() == Blocks.NETHER_BRICK))
                return genNetherBridge.getSpawnList();

            //modded
            for(final MapGenSpawningStructure structure : NetherAPIRegistry.NETHER.getStructures()) {
                final List<Biome.SpawnListEntry> possibleCreatures = structure.getPossibleCreatures(creatureType, world, pos);
                if(!possibleCreatures.isEmpty()) return possibleCreatures;
            }
        }

        return world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
        if(generateStructures) {
            //vanilla
            if("Fortress".equals(structureName) && genNetherBridge != null)
                return genNetherBridge.getNearestStructurePos(worldIn, position, findUnexplored);

            //modded
            for(final MapGenSpawningStructure structure : NetherAPIRegistry.NETHER.getStructures())
                if(structure.getStructureName().equals(structureName))
                    return structure.getNearestStructurePos(worldIn, position, findUnexplored);
        }

        return null;
    }

    @Override
    public boolean isInsideStructure(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos pos) {
        if(generateStructures) {
            if("Fortress".equals(structureName) && genNetherBridge != null) return genNetherBridge.isInsideStructure(pos);
            for(final MapGenSpawningStructure structure : NetherAPIRegistry.NETHER.getStructures())
                if(structure.getStructureName().equals(structureName)) return structure.isInsideStructure(pos);
        }

        return false;
    }

    @Override
    public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {
        if(generateStructures) {
            genNetherBridge.generate(world, x, z, null);
            NetherAPIRegistry.NETHER.getStructures().forEach(structure -> structure.generate(world, x, z, null));
        }
    }
}
