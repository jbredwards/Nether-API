/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.api.structure.ISpawningStructure;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.compat.netherex.NetherExHandler;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

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

    @SuppressWarnings({"DuplicateExpressions", "PointlessArithmeticExpression"})
    @Override
    public void prepareHeights(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) {
        final int lavaHeight = (world.getSeaLevel() >> 1) + 1;
        final int noiseHeight = (world.getActualHeight() >> 3) + 1;
        final int noiseWidthX = 5;
        final int noiseWidthZ = 5;

        buffer = getHeights(buffer, chunkX << 2, 0, chunkZ << 2, noiseWidthX, noiseHeight, noiseWidthZ);
        for(int j1 = 0; j1 < 4; ++j1) {
            for(int k1 = 0; k1 < 4; ++k1) {
                for(int heightIndex = noiseHeight - 2; heightIndex >= 0; heightIndex--) {
                    double d1 = buffer[((j1 + 0) * 5 + k1 + 0) * noiseHeight + heightIndex];
                    double d2 = buffer[((j1 + 0) * 5 + k1 + 1) * noiseHeight + heightIndex];
                    double d3 = buffer[((j1 + 1) * 5 + k1 + 0) * noiseHeight + heightIndex];
                    double d4 = buffer[((j1 + 1) * 5 + k1 + 1) * noiseHeight + heightIndex];
                    double d5 = (buffer[((j1 + 0) * 5 + k1 + 0) * noiseHeight + heightIndex + 1] - d1) * 0.125;
                    double d6 = (buffer[((j1 + 0) * 5 + k1 + 1) * noiseHeight + heightIndex + 1] - d2) * 0.125;
                    double d7 = (buffer[((j1 + 1) * 5 + k1 + 0) * noiseHeight + heightIndex + 1] - d3) * 0.125;
                    double d8 = (buffer[((j1 + 1) * 5 + k1 + 1) * noiseHeight + heightIndex + 1] - d4) * 0.125;

                    for(int yOffset = 0; yOffset < 8; yOffset++) {
                        double d10 = d1;
                        double d11 = d2;
                        final double d12 = (d3 - d1) * 0.25;
                        final double d13 = (d4 - d2) * 0.25;

                        for(int xOffset = 0; xOffset < 4; xOffset++) {
                            double density = d10;
                            final double d16 = (d11 - d10) * 0.25;

                            for(int zOffset = 0; zOffset < 4; zOffset++) {
                                final int y = (heightIndex << 3) + yOffset;
                                IBlockState state = null;

                                if(y < lavaHeight) state = LAVA;
                                if(density > 0) state = NETHERRACK;

                                primer.setBlockState((j1 << 2) + xOffset, y, (k1 << 2) + zOffset, state); // this method is actually nullable
                                density += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
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
                    if(posY >= 4 - rand.nextInt(5)) primer.setBlockState(posX, posY + world.getActualHeight() - 5, posZ, BEDROCK);
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

        if(NetherAPIConfig.hellCaves) genNetherCaves.generate(world, x, z, primer);
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

        @Nonnull final BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        @Nonnull final Biome biome = world.getBiome(pos.add(16, 0, 16));
        @Nonnull final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

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

    @Override
    public void populateWithVanilla(int chunkX, int chunkZ) {
        @Nonnull final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        final int originX = chunkX << 4;
        final int originZ = chunkZ << 4;
        final int maxHeight = world.getActualHeight();
        final int heightGenFactor = 0; // = maxHeight >> 8; // generate everything twice as often if the world height is twice what it normally would be

        BlockFalling.fallInstantly = true;
        ForgeEventFactory.onChunkPopulate(true, this, world, rand, chunkX, chunkZ, false);
        if(genNetherBridge != null) genNetherBridge.generateStructure(world, rand, chunkPos);

        @Nonnull final BlockPos pos = new BlockPos(originX, 0, originZ);
        @Nonnull final Biome biome = world.getBiome(pos.add(16, 0, 16));

        // lava "waterfalls"
        if(TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
            for(int i = 0; i < 8 << heightGenFactor; ++i) hellSpringGen.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight - 8) + 4, rand.nextInt(16) + 8));

        // fire
        if(TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.FIRE))
            for(int i = 0; i < (rand.nextInt(rand.nextInt(10) + 1) + 1) << heightGenFactor; ++i) fireFeature.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight - 8) + 4, rand.nextInt(16) + 8));

        // glowstone
        if(TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.GLOWSTONE)) {
            for(int i = 0; i < (rand.nextInt(rand.nextInt(10) + 1) + 1) << heightGenFactor; ++i) lightGemGen.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight - 8) + 4, rand.nextInt(16) + 8));
            for(int i = 0; i < 10 << heightGenFactor; ++i) hellPortalGen.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight), rand.nextInt(16) + 8));
        }

        ForgeEventFactory.onChunkPopulate(false, this, world, rand, chunkX, chunkZ, false);
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, chunkPos));

        // mushrooms
        if(TerrainGen.decorate(world, rand, chunkPos, DecorateBiomeEvent.Decorate.EventType.SHROOM)) {
            for(int i = 0; i < 1 << heightGenFactor; i++) {
                if(rand.nextBoolean()) brownMushroomFeature.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight), rand.nextInt(16) + 8));
                if(rand.nextBoolean()) redMushroomFeature.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight), rand.nextInt(16) + 8));
            }
        }

        // quartz gen
        if(TerrainGen.generateOre(world, rand, quartzGen, pos, OreGenEvent.GenerateMinable.EventType.QUARTZ))
            for(int i = 0; i < 16 << heightGenFactor; ++i) quartzGen.generate(world, rand, pos.add(rand.nextInt(16), rand.nextInt(maxHeight - 20) + 10, rand.nextInt(16)));

        // magma
        if(TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
            for(int i = 0; i < 4; ++i) magmaGen.generate(world, rand, pos.add(rand.nextInt(16), (world.getSeaLevel() >> 1) - 4 + rand.nextInt(10), rand.nextInt(16)));

        // lava traps
        if(TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
            for(int i = 0; i < 16 << heightGenFactor; ++i) lavaTrapGen.generate(world, rand, pos.add(rand.nextInt(16) + 8, rand.nextInt(maxHeight - 20) + 10, rand.nextInt(16) + 8));
        
        biome.decorate(world, rand, new BlockPos(originX, 0, originZ));
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, chunkPos));
        BlockFalling.fallInstantly = false;
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
    public void setBlocksInPrimer(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) { prepareHeights(chunkX, chunkZ, primer); }
}
