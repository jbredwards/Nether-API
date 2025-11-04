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

import net.minecraftforge.common.BiomeManager;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Having your nether or end biome implement this will allow it to have sub-biomes and edge biomes.
 *
 * @since 1.3.0
 * @author jbred
 *
 */
public interface INetherAPIBiomeProvider
{
    /**
     * @return all possible biomes that can spawn inside this one.
     */
    @Nonnull
    default List<BiomeManager.BiomeEntry> getSubBiomes() { return Collections.emptyList(); }

    /**
     * @return all possible biomes that can spawn along the edge of this one.
     */
    @Nonnull
    default List<BiomeManager.BiomeEntry> getEdgeBiomes(int neighborBiomeId) { return Collections.emptyList(); }
}
