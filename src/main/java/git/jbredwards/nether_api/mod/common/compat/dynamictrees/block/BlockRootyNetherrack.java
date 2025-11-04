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

package git.jbredwards.nether_api.mod.common.compat.dynamictrees.block;

import com.ferreusveritas.dynamictrees.blocks.MimicProperty;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import git.jbredwards.nether_api.api.util.PlantUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class BlockRootyNetherrack extends AbstractBlockRooty
{
    @Nonnull
    public static final BlockRootyNetherrack INSTANCE = new BlockRootyNetherrack();
    private BlockRootyNetherrack() { super("rooty_netherrack", Material.ROCK, false); }

    @Nonnull
    @Override
    public EnumPlantType getSoilType() { return PlantUtils.NETHERRACK_PLANT_TYPE; }

    @Nonnull
    @Override
    protected Block getFallbackMimic() { return Blocks.NETHERRACK; }

    @Nonnull
    @Override
    protected IBlockState getFallbackMimic(@Nonnull final IBlockAccess access, @Nonnull final BlockPos pos) {
        return MimicProperty.getGenericMimic(access, pos, null, DirtHelper.getSoilFlags("netherlike"), super.getFallbackMimic(access, pos));
    }
}
