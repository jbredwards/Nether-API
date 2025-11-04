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

package git.jbredwards.nether_api.api.structure;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Default implementation of {@link ISpawningStructure}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public abstract class MapGenSpawningStructure extends MapGenStructure implements ISpawningStructure
{
    @Nonnull
    public final Map<EnumCreatureType, List<Biome.SpawnListEntry>> spawnableCreatures = new EnumMap<>(EnumCreatureType.class);
    public void addSpawnableCreature(@Nonnull EnumCreatureType type, @Nonnull Biome.SpawnListEntry... entries) {
        spawnableCreatures.computeIfAbsent(type, typeIn -> new LinkedList<>()).addAll(Arrays.asList(entries));
    }

    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType type, @Nonnull World world, @Nonnull BlockPos pos) {
        if(!spawnableCreatures.isEmpty()) {
            final List<Biome.SpawnListEntry> creatures = spawnableCreatures.computeIfAbsent(type, typeIn -> new LinkedList<>());
            if(!creatures.isEmpty() && (isInsideStructure(pos) || isPositionInStructure(world, pos)) && isBlockBelowValidForSpawns(type, world, pos.down()))
                return creatures;
        }

        return Collections.emptyList();
    }

    protected abstract boolean isBlockBelowValidForSpawns(@Nonnull EnumCreatureType type, @Nonnull World world, @Nonnull BlockPos pos);
}
