/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.journey_into_the_light;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.journey.dimension.nether.JNWorldGenerator;
import net.journey.dimension.nether.biomes.*;
import net.journey.dimension.nether.biomes.structure.IStructureWorld;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 *
 * @author jbred
 *
 */
public final class JITLHandler
{
    @Nonnull static final Map<Class<? extends NetherBiome>, BiomeJITL> BIOME_LOOKUP = new HashMap<>(5);
    @Nonnull static final Method DOWN_RAY_METHOD = ObfuscationReflectionHelper.findMethod(JNWorldGenerator.class, "downRay", BlockPos.class, Chunk.class, BlockPos.class);
    @Nonnull static final Field PLANT_DENSITY_FIELD = ObfuscationReflectionHelper.findField(JNWorldGenerator.class, "plantDensity");

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        if(BiomeRegister.BIOME_FOREST != null) registry.registerBiome(getBiomeFromLookup(BiomeRegister.BIOME_FOREST), NetherAPIConfig.JITL.bloodForestWeight);
        if(BiomeRegister.BIOME_EARTHEN != null) registry.registerBiome(getBiomeFromLookup(BiomeRegister.BIOME_EARTHEN), NetherAPIConfig.JITL.earthenSeepWeight);
        if(BiomeRegister.BIOME_HEAT_SANDS != null) registry.registerBiome(getBiomeFromLookup(BiomeRegister.BIOME_HEAT_SANDS), NetherAPIConfig.JITL.heatSandsWeight);
    }

    public static void init() {
        if(BiomeRegister.BIOME_EMPTY_NETHER != null) getBiomeFromLookup(BiomeRegister.BIOME_EMPTY_NETHER).netherBiome = BiomeRegister.BIOME_EMPTY_NETHER;
        if(BiomeRegister.BIOME_FOREST != null) getBiomeFromLookup(BiomeRegister.BIOME_FOREST).netherBiome = BiomeRegister.BIOME_FOREST;
        if(BiomeRegister.BIOME_EARTHEN != null) getBiomeFromLookup(BiomeRegister.BIOME_EARTHEN).netherBiome = BiomeRegister.BIOME_EARTHEN;
        if(BiomeRegister.BIOME_HEAT_SANDS != null) getBiomeFromLookup(BiomeRegister.BIOME_HEAT_SANDS).netherBiome = BiomeRegister.BIOME_HEAT_SANDS;
        if(BiomeRegister.BIOME_FOREST_EDGE != null) getBiomeFromLookup(BiomeRegister.BIOME_FOREST_EDGE).netherBiome = BiomeRegister.BIOME_FOREST_EDGE;
    }

    @Nonnull
    public static BiomeJITL getBiomeFromLookup(@Nonnull NetherBiome netherBiome) {
        final BiomeJITL biome = BIOME_LOOKUP.get(netherBiome.getClass());
        if(biome != null) return biome;

        //should never pass
        throw new IllegalStateException("No Biome found for Journey Into The Light: {" + netherBiome.getName() + '}');
    }

    @SubscribeEvent
    static void registerBiomes(@Nonnull RegistryEvent.Register<Biome> event) {
        final BiConsumer<Class<? extends NetherBiome>, String> registerAction = (netherBiomeClass, biomeName) -> {
            final BiomeJITL biome = new BiomeJITL(netherBiomeClass, biomeName);
            event.getRegistry().register(biome);

            BIOME_LOOKUP.put(netherBiomeClass, biome);
            BiomeDictionary.addTypes(biome, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY);
        };

        //register biomes
        registerAction.accept(NetherBiome.class, "Empty Nether");
        registerAction.accept(NetherBiomeForest.class, "Blood Forest");
        registerAction.accept(NetherBiomeEarthen.class, "Earthen Seep");
        registerAction.accept(NetherBiomeHeatSands.class, "Heat Sands");
        registerAction.accept(NetherBiomeForestEdge.class, "Blood Forest Edge");
    }

    // largely copied from JNWorldGenerator::generate, but with fixes for cascading world gen bugs & optimizations
    // this method will likely be moved to BiomeJITL::buildSurface in the future
    @SubscribeEvent
    static void globalNetherPopulations(@Nonnull PopulateChunkEvent.Post event) throws InvocationTargetException, IllegalAccessException {
        if(event.getWorld().provider.getDimensionType() == DimensionType.NETHER && event.getWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            BlockFalling.fallInstantly = true;

            // -----------------
            // GLOBAL STRUCTURES
            // -----------------

            if((JNWorldGenerator.globalStructuresLand.length != 0
            || JNWorldGenerator.globalStructuresLava.length != 0
            || JNWorldGenerator.globalStructuresCave.length != 0)
            && event.getRand().nextInt(16) == 0) {
                final int x = (event.getChunkX() << 4) + event.getRand().nextInt(16) + 8;
                final int z = (event.getChunkZ() << 4) + event.getRand().nextInt(16) + 8;

                final BlockPos.MutableBlockPos start = new BlockPos.MutableBlockPos(x, 32 + event.getRand().nextInt(88), z);
                final Chunk chunk = event.getWorld().getChunk(start);

                while(start.getY() > 32 && !chunk.getBlockState(start).getBlock().isAir(chunk.getBlockState(start), event.getWorld(), start)) start.setY(start.getY() - 1);
                final BlockPos rayPos = (BlockPos)DOWN_RAY_METHOD.invoke(null, chunk, start);

                if(rayPos != null) {
                    boolean terrain = true;
                    for(int y = 1; y < 8; ++y) {
                        if(!chunk.getBlockState(rayPos.up(y)).getBlock().isAir(chunk.getBlockState(rayPos.up(y)), event.getWorld(), rayPos.up(y))) {
                            terrain = false;
                            break;
                        }
                    }

                    if(terrain) {
                        if(JNWorldGenerator.globalStructuresLava.length != 0 && chunk.getBlockState(rayPos).getMaterial() == Material.LAVA) {
                            final IStructureWorld structure = JNWorldGenerator.globalStructuresLava[event.getRand().nextInt(JNWorldGenerator.globalStructuresLava.length)];
                            structure.generateSurface(event.getWorld(), rayPos.up(), event.getRand());
                        }

                        else if(JNWorldGenerator.globalStructuresLand.length != 0) {
                            final IStructureWorld structure = JNWorldGenerator.globalStructuresLand[event.getRand().nextInt(JNWorldGenerator.globalStructuresLand.length)];
                            structure.generateSurface(event.getWorld(), rayPos.up(), event.getRand());
                        }
                    }

                    else if(JNWorldGenerator.globalStructuresCave.length != 0) {
                        final IStructureWorld structure = JNWorldGenerator.globalStructuresCave[event.getRand().nextInt(JNWorldGenerator.globalStructuresCave.length)];
                        structure.generateSubterrain(event.getWorld(), rayPos, event.getRand());
                    }
                }
            }

            // --------------
            // BIOME SURFACES
            // --------------

            final float plantDensity;
            try { plantDensity = PLANT_DENSITY_FIELD.getFloat(null); }
            catch(final IllegalAccessException e) { throw new RuntimeException(e); } // should never pass

            final Chunk[] chunks = new Chunk[4];
            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(event.getChunkX() << 4 | 8, 0, event.getChunkZ() << 4 | 8);

            for(int x = 0; x < 16; x++, pos.x++) {
                for(int z = 0; z < 16; z++, pos.z++) {
                    final int index = (z & 1) << 1 | (x & 1);
                    final Chunk chunk = chunks[index] != null ? chunks[index] : (chunks[index] = event.getWorld().getChunk(pos));
                    final Biome biome = chunk.getBiome(pos, event.getWorld().getBiomeProvider());

                    if(biome instanceof BiomeJITL) {
                        final NetherBiome netherBiome = ((BiomeJITL)biome).netherBiome;
                        if(netherBiome != null) {
                            for(int y = 5; y < 126; y++) {
                                pos.setY(y);

                                if(chunk.getBlockState(pos).isFullCube()) {
                                    if(chunk.getBlockState(pos.up()).getMaterial() == Material.AIR) {
                                        netherBiome.genSurfColumn(chunk, pos, event.getRand());
                                        if(event.getRand().nextFloat() < plantDensity) netherBiome.genFloorObjects(chunk, pos, event.getRand());
                                    }

                                    else if(chunk.getBlockState(pos.down()).getMaterial() == Material.AIR) {
                                        if(event.getRand().nextFloat() < plantDensity) netherBiome.genCeilObjects(chunk, pos, event.getRand());
                                    }

                                    // NetherBiome::genWallObjects is unused in JITL, no need to implement logic for it here.
                                }
                            }
                        }
                    }
                }
            }

            BlockFalling.fallInstantly = false;
        }
    }
}
