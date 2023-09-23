/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.structure.ISpawningStructure;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * TODO, will use GenLayer to determine the placement of end islands and their biomes (one biome per end island)
 * @author jbred
 *
 */
public class ChunkGeneratorTheEnd extends ChunkGeneratorEnd implements INetherAPIChunkGenerator
{
    @Nonnull
    protected final List<MapGenStructure> moddedStructures = new LinkedList<>();
    public ChunkGeneratorTheEnd(@Nonnull World worldIn, boolean generateStructures, long seed, @Nonnull BlockPos spawnCoord) {
        super(worldIn, generateStructures, seed, spawnCoord);
        NetherAPIRegistry.THE_END.getStructures().forEach(entry -> moddedStructures.add(entry.getStructureFactory().apply(this)));
    }



    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
        if(areStructuresEnabled()) {
            for(final MapGenStructure structure : moddedStructures) {
                if(structure instanceof ISpawningStructure) {
                    final List<Biome.SpawnListEntry> possibleCreatures = ((ISpawningStructure)structure).getPossibleCreatures(creatureType, world, pos);
                    if(!possibleCreatures.isEmpty()) return possibleCreatures;
                }
            }
        }

        return world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
        if(areStructuresEnabled()) {
            //vanilla
            if("EndCity".equals(structureName) && endCityGen != null)
                return endCityGen.getNearestStructurePos(worldIn, position, findUnexplored);

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
            if("EndCity".equals(structureName) && endCityGen != null) return endCityGen.isInsideStructure(pos);

            //modded
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
    public void populateWithVanilla(int chunkX, int chunkZ) { super.populate(chunkX, chunkZ); }

    @Override
    public void setBlocksInPrimer(int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) { setBlocksInChunk(chunkX, chunkZ, primer); }
}
