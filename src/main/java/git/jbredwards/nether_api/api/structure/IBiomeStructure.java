/*
 * Copyright (C) <2026 to Present> <jbredwards>
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Should be implemented by {@link net.minecraft.world.gen.structure.StructureStart StructureStart}
 * and {@link net.minecraft.world.gen.structure.StructureComponent StructureComponent}
 * instances that support {@link git.jbredwards.nether_api.api.event.BiomeStructureEvent BiomeStructureEvent}.
 *
 * @see MapGenSpawningStructure
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public interface IBiomeStructure
{
    /**
     * Some general guidelines for {@code StructureComponents} implementing this class:<br>
     * - The starting structure component should get this value using a {@code BiomeProvider}, rather than
     * {@code World} (to avoid loading un-generated chunks). Alternatively, structure components can use the
     * {@code StructureStart} to get this value.<br>
     * - All child structure components should retrieve this value from the starting structure component, and
     * are all expected to have the same value.<br>
     * - All structure components should read/write this value using nbt.
     *
     * @return The origin biome for this structure.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    Biome getBiome();

    /**
     * A version of
     * {@link net.minecraft.world.gen.structure.StructureVillagePieces.Village#getBiomeSpecificBlockState Village.getBiomeSpecificBlockState}
     * that works for any {@code IBiomeStructure}.
     * @throws NullPointerException If structureName or state are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    default IBlockState getBiomeSpecificBlockState(@Nonnull final String structureName, @Nonnull final IBlockState state) {
        Objects.requireNonNull(structureName);
        Objects.requireNonNull(state);

        @Nullable final Biome biome = this.getBiome();
        return biome != null ? BiomeStructureEvent.getBlockId(biome, structureName, state) : state;
    }

    /**
     * A version of {@link IBiomeStructure#getBiomeSpecificBlockState} that can be passed into structures using {@link Template}.
     * @throws NullPointerException If structureName is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    default ITemplateProcessor getBiomeSpecificTemplateProcessor(@Nonnull final String structureName) {
        Objects.requireNonNull(structureName);

        @Nullable final Biome biome = this.getBiome();
        return biome == null ? null : (world, pos, blockInfo) -> {
            @Nonnull final IBlockState newState = BiomeStructureEvent.getBlockId(biome, structureName, blockInfo.blockState);
            return newState == blockInfo.blockState ? blockInfo : new Template.BlockInfo(blockInfo.pos, newState, blockInfo.tileentityData);
        };
    }
}
