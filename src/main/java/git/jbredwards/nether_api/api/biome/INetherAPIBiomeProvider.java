/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
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
