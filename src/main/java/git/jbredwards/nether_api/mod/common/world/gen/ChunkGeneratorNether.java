/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.structure.ISpawningStructure;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenMinable;
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
public class ChunkGeneratorNether extends ChunkGeneratorHell implements INetherAPIChunkGenerator
{
    @Nonnull protected final NoiseGeneratorPerlin terrainNoiseGen;
    @Nonnull protected Biome[] biomesForGeneration = new Biome[0];

    @Nonnull
    protected final List<MapGenStructure> moddedStructures = new LinkedList<>();
    public ChunkGeneratorNether(@Nonnull World worldIn, boolean generateStructures, long seed) {
        super(worldIn, generateStructures, seed);
        terrainNoiseGen = new NoiseGeneratorPerlin(rand, 4);
        magmaGen = new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33,
                state -> state == NETHERRACK || state.isFullCube() && state.getMaterial() != Material.ROCK);

        NetherAPIRegistry.NETHER.getStructures().forEach(entry -> moddedStructures.add(entry.getStructureFactory().apply(this)));
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

        final double[] terrainNoise = terrainNoiseGen.getRegion(null, originX, originZ, 16, 16, 0.0625, 0.0625, 1);
        for(int posX = 0; posX < 16; posX++) {
            for(int posZ = 0; posZ < 16; posZ++) {
                //generate the bedrock
                for(int posY = 4; posY >= 0; posY--) {
                    if(posY <= rand.nextInt(5)) primer.setBlockState(posX, posY, posZ, BEDROCK);
                    if(posY >= 4 - rand.nextInt(5)) primer.setBlockState(posX, posY + 123, posZ, BEDROCK);
                }

                //replace netherrack top and filler blocks, and generate random soul sand & gravel
                final Biome biome = biomesForGeneration[posZ << 4 | posX];
                if(biome instanceof INetherBiome) ((INetherBiome)biome).buildSurface(this, chunkX, chunkZ, primer, posX, posZ, slowsandNoise, gravelNoise, depthBuffer, terrainNoise[posZ << 4 | posX]);
                else NetherGenerationUtils.buildSurfaceAndSoulSandGravel(world, rand, primer, posX, posZ, slowsandNoise, gravelNoise, depthBuffer, NETHERRACK, biome.topBlock, biome.fillerBlock, LAVA);

                //debugging
                //final Biome biome = biomesForGeneration[posZ << 4 | posX];
                //primer.setBlockState(posX, 9, posZ, biome.topBlock);
                //primer.setBlockState(posX, 8, posZ, BEDROCK);
            }
        }
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int x, int z) {
        rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        biomesForGeneration = world.getBiomeProvider().getBiomes(null, x << 4, z << 4, 16, 16);

        final ChunkPrimer primer = new ChunkPrimer();
        setBlocksInPrimer(x, z, primer);
        buildSurfaces(x, z, primer);
        genNetherCaves.generate(world, x, z, primer);

        if(areStructuresEnabled()) {
            if(genNetherBridge != null) genNetherBridge.generate(world, x, z, primer);
            moddedStructures.forEach(structure -> structure.generate(world, x, z, primer));
        }

        final Chunk chunk = new Chunk(world, primer, x, z);
        byte[] biomeArray = chunk.getBiomeArray();
        for(int i = 0; i < biomeArray.length; ++i) biomeArray[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);

        chunk.resetRelightChecks();
        if(NetherAPI.isNetherExLoaded) NetherExHandler.onChunkGenerate(chunk);

        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        //ensure forge's fix is active when this runs, otherwise the console gets spammed
        //if you're using this mod, you don't care about the nether being 1:1 with vanilla 1.12 lol
        final boolean prevFixVanillaCascading = ForgeModContainer.fixVanillaCascading;
        ForgeModContainer.fixVanillaCascading = true;

        final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        final Biome biome = world.getBiome(pos.add(16, 0, 16));
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        moddedStructures.forEach(structure -> structure.generateStructure(world, rand, chunkPos));
        if(!(biome instanceof INetherBiome)) populateWithVanilla(chunkX, chunkZ);
        else { //allow mods to populate chunks differently
            BlockFalling.fallInstantly = true;

            if(genNetherBridge != null) genNetherBridge.generateStructure(world, rand, chunkPos);
            ((INetherBiome)biome).populate(this, chunkX, chunkZ);

            BlockFalling.fallInstantly = false;
        }

        //restore old vanilla cascading fix settings
        ForgeModContainer.fixVanillaCascading = prevFixVanillaCascading;
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
        if(areStructuresEnabled()) {
            //vanilla
            if(creatureType == EnumCreatureType.MONSTER && genNetherBridge != null
            && (genNetherBridge.isInsideStructure(pos) || genNetherBridge.isPositionInStructure(world, pos) && world.getBlockState(pos.down()).getBlock() == Blocks.NETHER_BRICK))
                return genNetherBridge.getSpawnList();

            //modded
            else for(final MapGenStructure structure : moddedStructures) {
                if(structure instanceof ISpawningStructure) {
                    final List<Biome.SpawnListEntry> possibleCreatures = ((ISpawningStructure)structure).getPossibleCreatures(creatureType, world, pos);
                    if(!possibleCreatures.isEmpty()) return possibleCreatures;
                }
            }
        }

        return NetherAPI.isNetherExLoaded ? NetherExHandler.getSpawnableList(world.getBiome(pos), creatureType) : world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
        if(areStructuresEnabled()) {
            //vanilla
            if("Fortress".equals(structureName) && genNetherBridge != null)
                return genNetherBridge.getNearestStructurePos(worldIn, position, findUnexplored);

            //modded
            else for(final MapGenStructure structure : moddedStructures)
                if(structure.getStructureName().equals(structureName)) return structure.getNearestStructurePos(worldIn, position, findUnexplored);
        }

        return null;
    }

    @Override
    public boolean isInsideStructure(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos pos) {
        if(areStructuresEnabled()) {
            //vanilla
            if("Fortress".equals(structureName) && genNetherBridge != null) return genNetherBridge.isInsideStructure(pos);

            //modded
            else for(final MapGenStructure structure : moddedStructures)
                if(structure.getStructureName().equals(structureName)) return structure.isInsideStructure(pos);
        }

        return false;
    }

    @Override
    public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {
        if(areStructuresEnabled()) {
            genNetherBridge.generate(world, x, z, null);
            moddedStructures.forEach(structure -> structure.generate(world, x, z, null));
        }
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
    public boolean areStructuresEnabled() { return generateStructures; }

    @Override
    public void populateWithVanilla(int chunkX, int chunkZ) { super.populate(chunkX, chunkZ); }

    @Override
    public void setBlocksInPrimer(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) { prepareHeights(chunkX, chunkZ, primer); }
}
