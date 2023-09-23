/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.biome.IExtendedBiome;
import biomesoplenty.api.enums.BOPClimates;
import biomesoplenty.common.world.WorldTypeBOP;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class BiomesOPlentyHandler
{
    /**
     * Consistent behavior with BOP, but with the config option to override it.
     */
    public static boolean allowBOPNetherBiomes(@Nonnull World world) {
        return !NetherAPIConfig.BOP.dependentBOPHellBiomes || world.getWorldType() instanceof WorldTypeBOP;
    }

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry, @Nonnull World world) {
        if(allowBOPNetherBiomes(world)) { // only use BOP biomes if the world type allows it
            BOPBiomes.corrupted_sands.toJavaUtil().ifPresent(biome ->
                    registry.registerBiome(biome, ((IExtendedBiome)biome).getWeightMap().getOrDefault(BOPClimates.HELL, 0)));
            BOPBiomes.fungi_forest.toJavaUtil().ifPresent(biome ->
                    registry.registerBiome(biome, ((IExtendedBiome)biome).getWeightMap().getOrDefault(BOPClimates.HELL, 0)));
            BOPBiomes.phantasmagoric_inferno.toJavaUtil().ifPresent(biome ->
                    registry.registerBiome(biome, ((IExtendedBiome)biome).getWeightMap().getOrDefault(BOPClimates.HELL, 0)));
            BOPBiomes.undergarden.toJavaUtil().ifPresent(biome ->
                    registry.registerBiome(biome, ((IExtendedBiome)biome).getWeightMap().getOrDefault(BOPClimates.HELL, 0)));
            BOPBiomes.visceral_heap.toJavaUtil().ifPresent(biome ->
                    registry.registerBiome(biome, ((IExtendedBiome)biome).getWeightMap().getOrDefault(BOPClimates.HELL, 0)));
        }
    }
}
