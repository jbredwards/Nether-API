/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.structure.ISpawningStructure;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import net.minecraft.block.BlockChorusFlower;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public class ChunkGeneratorTheEnd extends ChunkGeneratorEnd implements INetherAPIChunkGenerator
{
    @Nonnull protected final NoiseGeneratorPerlin terrainNoiseGen;
    @Nonnull protected final MapGenCavesEnd genEndCaves = new MapGenCavesEnd();

    @Nonnull protected final List<MapGenStructure> moddedStructures = new LinkedList<>();
    @Nonnull protected Biome[] biomesForGeneration = new Biome[0];

    public ChunkGeneratorTheEnd(@Nonnull World worldIn, boolean generateStructures, @Nonnull BiomeProviderTheEnd biomeProvider, @Nonnull BlockPos spawnCoord) {
        super(worldIn, generateStructures, worldIn.getSeed(), spawnCoord);
        terrainNoiseGen = new NoiseGeneratorPerlin(rand, 4);
        islandNoise = biomeProvider.islandNoise;

        NetherAPIRegistry.THE_END.getStructures().forEach(entry -> moddedStructures.add(entry.getStructureFactory().apply(this)));
    }

    public void buildSurfaces(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) {
        if(!ForgeEventFactory.onReplaceBiomeBlocks(this, chunkX, chunkZ, primer, world)) return;
        final double[] terrainNoise = terrainNoiseGen.getRegion(null, chunkX << 4, chunkZ << 4, 16, 16, 0.0625, 0.0625, 1);
        for(int posX = 0; posX < 16; posX++) {
            for(int posZ = 0; posZ < 16; posZ++) {
                final Biome biome = biomesForGeneration[posZ << 4 | posX];
                if(biome instanceof IEndBiome) ((IEndBiome)biome).buildSurface(this, chunkX, chunkZ, primer, posX, posZ, terrainNoise[posZ << 4 | posX]);

                // for debugging:
                // if(biome == Biomes.VOID) primer.setBlockState(posX, 0, posZ, Blocks.GLASS.getDefaultState());
                // else primer.setBlockState(posX, 0, posZ, Blocks.WOOL.getDefaultState());
            }
        }
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
        biomesForGeneration = world.getBiomeProvider().getBiomes(null, chunkX << 4, chunkZ << 4, 16, 16);

        final ChunkPrimer primer = new ChunkPrimer();
        setBlocksInPrimer(chunkX, chunkZ, primer);
        buildSurfaces(chunkX, chunkZ, primer);

        if(NetherAPIConfig.endCaves) genEndCaves.generate(world, chunkX, chunkZ, primer);
        if(areStructuresEnabled()) {
            if(endCityGen != null) endCityGen.generate(world, chunkX, chunkZ, primer);
            moddedStructures.forEach(structure -> structure.generate(world, chunkX, chunkZ, primer));
        }

        final Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        for(int i = 0; i < biomeArray.length; ++i) biomeArray[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        // ensure forge's fix is active when this runs, otherwise world gen could load neighboring chunks
        // if you're using this mod, you don't care about the end being 1:1 with vanilla 1.12 lol
        final boolean prevFixVanillaCascading = ForgeModContainer.fixVanillaCascading;
        ForgeModContainer.fixVanillaCascading = true;

        final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        final Biome biome = world.getBiome(pos.add(16, 0, 16));
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        moddedStructures.forEach(structure -> structure.generateStructure(world, rand, chunkPos));
        if(!(biome instanceof IEndBiome)) populateWithVanilla(chunkX, chunkZ);
        else { //allow mods to populate chunks differently
            BlockFalling.fallInstantly = true;

            if(endCityGen != null) endCityGen.generateStructure(world, rand, chunkPos);
            ((IEndBiome)biome).populate(this, chunkX, chunkZ);

            BlockFalling.fallInstantly = false;
        }

        // restore old vanilla cascading fix settings
        ForgeModContainer.fixVanillaCascading = prevFixVanillaCascading;
    }

    @Override
    public void populateWithVanilla(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        ForgeEventFactory.onChunkPopulate(true, this, world, rand, chunkX, chunkZ, false);
        if(areStructuresEnabled()) endCityGen.generateStructure(world, rand, new ChunkPos(chunkX, chunkZ));

        final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        final Biome biome = world.getBiome(pos.add(16, 0, 16));
        
        if((long)chunkX * (long)chunkX + (long)chunkZ * (long)chunkZ > 4096L) {
            final float height = getIslandHeightValue(chunkX, chunkZ, 1, 1);

            // small floating end island platforms
            if((!(biome instanceof IEndBiome) || ((IEndBiome)biome).generateIslands(this, chunkX, chunkZ, height)) && height < -20 && rand.nextInt(14) == 0) {
                endIslands.generate(world, rand, pos.add(rand.nextInt(16) + 8, 55 + rand.nextInt(16), rand.nextInt(16) + 8));
                if(rand.nextInt(4) == 0) endIslands.generate(world, rand, pos.add(rand.nextInt(16) + 8, 55 + rand.nextInt(16), rand.nextInt(16) + 8));
            }

            // chorus plants
            if((!(biome instanceof IEndBiome) || ((IEndBiome)biome).generateChorusPlants(this, chunkX, chunkZ, height)) && height > 40) {
                final int amountInChunk = rand.nextInt(5);
                for(int i = 0; i < amountInChunk; i++) {
                    final int xOffset = rand.nextInt(16) + 8;
                    final int zOffset = rand.nextInt(16) + 8;
                    final int y = world.getHeight(pos.add(xOffset, 0, zOffset)).getY();

                    if(y > 0 && world.isAirBlock(pos.add(xOffset, y, zOffset))) {
                        final IBlockState ground = world.getBlockState(pos.add(xOffset, y - 1, zOffset));
                        if(ground == END_STONE) BlockChorusFlower.generatePlant(world, pos.add(xOffset, y, zOffset), rand, 8);
                    }
                }
            }

            // end gateways
            if(height > 40 && rand.nextInt(700) == 0) {
                final int xOffset = rand.nextInt(16) + 8;
                final int zOffset = rand.nextInt(16) + 8;
                final int blockHeight = world.getHeight(pos.add(xOffset, 0, zOffset)).getY();

                if(blockHeight > 0) {
                    final BlockPos offset = pos.add(xOffset, blockHeight + 3 + rand.nextInt(7), zOffset);
                    WorldProviderTheEnd.END_GATEWAY.generate(world, rand, offset);
                    
                    final TileEntity tile = world.getTileEntity(offset);
                    if(tile instanceof TileEntityEndGateway) ((TileEntityEndGateway)tile).setExactPosition(spawnPoint);
                }
            }
        }
        
        biome.decorate(world, rand, pos);
        ForgeEventFactory.onChunkPopulate(false, this, world, rand, chunkX, chunkZ, false);
        BlockFalling.fallInstantly = false;
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
        // modded
        if(areStructuresEnabled()) {
            for(final MapGenStructure structure : moddedStructures) {
                if(structure instanceof ISpawningStructure) {
                    final List<Biome.SpawnListEntry> possibleCreatures = ((ISpawningStructure)structure).getPossibleCreatures(creatureType, world, pos);
                    if(!possibleCreatures.isEmpty()) return possibleCreatures;
                }
            }
        }

        // vanilla
        return world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
        if(areStructuresEnabled()) {
            // vanilla
            if("EndCity".equals(structureName) && endCityGen != null)
                return endCityGen.getNearestStructurePos(worldIn, position, findUnexplored);

            // modded
            else for(final MapGenStructure structure : moddedStructures)
                if(structure.getStructureName().equals(structureName)) return structure.getNearestStructurePos(worldIn, position, findUnexplored);
        }

        return null;
    }


    @Override
    public boolean isInsideStructure(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos pos) {
        if(areStructuresEnabled()) {
            // vanilla
            if("EndCity".equals(structureName) && endCityGen != null) return endCityGen.isInsideStructure(pos);

            // modded
            else for(final MapGenStructure structure : moddedStructures)
                if(structure.getStructureName().equals(structureName)) return structure.isInsideStructure(pos);
        }

        return false;
    }

    @Override
    public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {
        if(areStructuresEnabled()) moddedStructures.forEach(structure -> structure.generate(world, x, z, null));
    }

    // ========================
    // INetherAPIChunkGenerator
    // ========================

    @Nonnull
    @Override
    public World getWorld() { return world; }

    @Nonnull
    @Override
    public Random getRand() { return rand; }

    @Override
    public boolean areStructuresEnabled() { return mapFeaturesEnabled; }

    @Override
    public void setBlocksInPrimer(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) { setBlocksInChunk(chunkX, chunkZ, primer); }
}
