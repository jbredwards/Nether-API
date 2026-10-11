/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.api.biome;

import org.jetbrains.annotations.ApiStatus;

/**
 * Players cannot randomly spawn into biomes that implement this class.
 * For example: players can randomly spawn into a forest biome but not an ocean.
 * <p>
 * Note: this is currently only used for biome providers that extend
 * {@link git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderNetherAPI BiomeProviderNetherAPI}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.3.0")
public interface INoSpawnBiome
{
    /**
     * @return Whether this biome denies player spawning.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    default boolean denyPlayerSpawn() {
        return true;
    }
}
