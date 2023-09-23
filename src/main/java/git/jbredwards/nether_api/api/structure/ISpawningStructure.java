/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.structure;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Having your MapGenStructure implement this will allow it go passively spawn mods (similar to how nether fortresses do).
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ISpawningStructure
{
    /**
     * @return all possible spawn entries for the provided creature type. This should also check whether the provided position is within the structure.
     */
    @Nonnull
    List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType type, @Nonnull World world, @Nonnull BlockPos pos);
}
