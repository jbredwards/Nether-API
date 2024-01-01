/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.biome;

/**
 * Players cannot randomly spawn into biomes that implement this class.
 * For example: players can randomly spawn into a forest biome but not an ocean.
 * <p>
 * Note: this is currently only used for biome providers that extend
 * {@link git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNetherAPI BiomeProviderNetherAPI}.
 *
 * @since 1.3.0
 * @author jbred
 *
 */
public interface INoSpawnBiome { }
