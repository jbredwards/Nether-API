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

package git.jbredwards.nether_api.api.util;

import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;

/**
 * Compatibility utilities for modded plants and soils.
 *
 * @since 1.4.0
 * @author jbred
 *
 */
public final class PlantUtils
{
    /**
     * This PlantType is for any plants that can be planted on "End Stone"-like blocks.
     * <p>At runtime this is automatically applied to Vanilla's Chorus Plants.</p>
     */
    @Nonnull public static final EnumPlantType END_PLANT_TYPE = EnumPlantType.getPlantType("ender");

    /**
     * This PlantType is for any plants that can be planted on both "Netherrack"-like blocks and "Soul Sand"-like blocks.
     */
    @Nonnull public static final EnumPlantType NETHER_PLANT_TYPE = EnumPlantType.getPlantType(NetherAPI.MODID + ":nether");

    /**
     * This PlantType is for any plants that can be planted on "Netherrack"-like blocks.
     */
    @Nonnull public static final EnumPlantType NETHERRACK_PLANT_TYPE = EnumPlantType.getPlantType(NetherAPI.MODID + ":netherrack");

    /**
     * This PlantType is for any plants that can be planted on "Soul Sand"-like blocks.
     */
    @Nonnull public static final EnumPlantType SOUL_SAND_PLANT_TYPE = EnumPlantType.Nether;

    /**
     * Utility method that should be applied by every soil block that supports any of the three Nether PlantTypes above.
     * A use case for a "Soul Sand"-like block looks like this:
     * <blockquote><pre>
     * &#64;Override
     * public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
     *     return PlantUtils.sustainsNether(PlantUtils.SOUL_SAND_PLANT_TYPE, world, pos, direction, plantable)
     *            || super.canSustainPlant(state, world, pos, direction, plantable);
     * }
     * </pre></blockquote>
     * @return True if this soil type sustains the {@link IPlantable}.
     */
    public static boolean sustainsNether(@Nonnull final EnumPlantType soilType, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
        return sustainsNether(soilType, plantable.getPlantType(world, pos.offset(direction)));
    }

    /**
     * @return True if this soil type sustains the {@link EnumPlantType}.
     */
    public static boolean sustainsNether(@Nonnull final EnumPlantType soilType, @Nonnull final EnumPlantType plantType) {
        return plantType == soilType || (soilType == NETHER_PLANT_TYPE ? plantType == NETHERRACK_PLANT_TYPE || plantType == SOUL_SAND_PLANT_TYPE : plantType == NETHER_PLANT_TYPE);
    }

    /**
     * @return A new IPlantable instance using the plantType. This is useful when checking if a soil block can sustain
     * an {@link EnumPlantType}, without access to an {@link IPlantable}.
     */
    @Nonnull
    public static IPlantable createPlantable(@Nonnull final EnumPlantType plantType) {
        return createPlantable(plantType, Blocks.AIR.getDefaultState());
    }

    /**
     * @return A new IPlantable instance using the plant type and plant blockstate. This is useful when checking if a
     * soil block can sustain an {@link EnumPlantType}, without access to an {@link IPlantable}.
     */
    @Nonnull
    public static IPlantable createPlantable(@Nonnull final EnumPlantType plantType, @Nonnull final IBlockState plant) {
        return new IPlantable() {
            @Nonnull
            @Override
            public EnumPlantType getPlantType(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
                return plantType;
            }

            @Nonnull
            @Override
            public IBlockState getPlant(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos) {
                return plant;
            }
        };
    }
}
