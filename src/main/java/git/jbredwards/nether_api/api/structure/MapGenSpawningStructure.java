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

import git.jbredwards.nether_api.api.event.BiomeStructureEvent;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    /**
     * The default spawn entries for this structure.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @ApiStatus.Obsolete(since = "1.5.0")
    @Nonnull
    public final Map<EnumCreatureType, List<Biome.SpawnListEntry>> spawnableCreatures = new EnumMap<>(EnumCreatureType.class);

    /**
     * Use {@code getPossibleCreatures} for the actual spawn list.
     * @return The default spawn list for the provided creature type.
     * @throws NullPointerException If type is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    protected final List<Biome.SpawnListEntry> getSpawnableCreatures(@Nonnull final EnumCreatureType type) {
        return this.spawnableCreatures.computeIfAbsent(Objects.requireNonNull(type), typeIn -> new ArrayList<>());
    }

    /**
     * Adds spawn entries to the default spawn list.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    public void addSpawnableCreature(@Nonnull final EnumCreatureType type, @Nonnull final Biome.SpawnListEntry... entries) {
        this.getSpawnableCreatures(type).addAll(Arrays.asList(entries));
    }

    /**
     * @return Whether the block at the provided position is valid ground for creature spawning.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    protected abstract boolean isBlockBelowValidForSpawns(@Nonnull final EnumCreatureType type, @Nonnull final World world, @Nonnull final BlockPos pos);

    /**
     * @return A {@code StructureStart} that's able to spawn the provided creature type at the position.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    protected StructureStart getStructureForSpawns(@Nonnull final EnumCreatureType type, @Nonnull final World world, @Nonnull final BlockPos pos) {
        // If a StructureComponent encompasses the position, allow it to spawn creatures.
        @Nullable final StructureStart structure = this.getStructureAt(pos);
        if(structure != null) return structure;

        // Allow structures whose bounding box encompasses the position, but only if the block is valid.
        else for(@Nonnull final StructureStart candidate : this.structureMap.values()) {
            if(candidate.isSizeableStructure() && candidate.getBoundingBox().isVecInside(pos)) {
                return this.isBlockBelowValidForSpawns(type, world, pos.down()) ? candidate : null;
            }
        }

        // No structure at pos.
        return null;
    }

    /**
     * @return All possible spawn entries for the provided creature type.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull final EnumCreatureType type, @Nonnull final World world, @Nonnull final BlockPos pos) {
        this.initializeStructureData(world);

        @Nullable final StructureStart structure = this.getStructureForSpawns(type, world, pos);
        if(structure == null) return Collections.emptyList();
        else if(structure instanceof IBiomeStructure) {
            @Nullable final Biome biome = ((IBiomeStructure)structure).getBiome();
            if(biome != null) return BiomeStructureEvent.prepareSpawnList(biome, this.getStructureName(), type, () -> this.getSpawnableCreatures(type));
        }

        return this.getSpawnableCreatures(type);
    }
}
