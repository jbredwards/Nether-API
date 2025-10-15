/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.dynamictrees;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootyEndStone;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootyNetherrack;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootySoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Injected into the BOP hellbark dynamic trees species at runtime.
 * @author jbred
 *
 */
@SuppressWarnings("unused") //used via asm
public abstract class SpeciesPlantable extends Species
{
    public SpeciesPlantable() {}
    public SpeciesPlantable(@Nonnull final ResourceLocation name, @Nonnull final TreeFamily treeFamily) {
        super(name, treeFamily);
    }

    public SpeciesPlantable(@Nonnull final ResourceLocation name, @Nonnull final TreeFamily treeFamily, @Nonnull final ILeavesProperties leavesProperties) {
        super(name, treeFamily, leavesProperties);
    }

    @Nonnull
    @Override
    public BlockRooty getRootyBlock(@Nonnull final World world, @Nonnull final BlockPos rootPos) {
        if(canSustainPlant(world, rootPos, world.getBlockState(rootPos))) {
            @Nullable final BlockRooty soil = getRootyBlock(getSimulatedPlantable().getPlantType(world, rootPos.offset(getGrowthDirection())));
            if(soil != null) return soil;
        }

        return super.getRootyBlock(world, rootPos);
    }

    @Nullable
    protected BlockRooty getRootyBlock(@Nonnull final EnumPlantType soilType) {
        if(soilType == PlantUtils.NETHERRACK_PLANT_TYPE || soilType == PlantUtils.NETHER_PLANT_TYPE) return BlockRootyNetherrack.INSTANCE;
        if(soilType == PlantUtils.SOUL_SAND_PLANT_TYPE) return BlockRootySoulSand.INSTANCE;
        if(soilType == PlantUtils.END_PLANT_TYPE) return BlockRootyEndStone.INSTANCE;
        return null;
    }

    @Override
    public boolean isAcceptableSoil(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState soilBlockState) {
        return isAcceptableSoil(soilBlockState) || canSustainPlant(world, pos, soilBlockState);
    }

    public boolean canSustainPlant(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        return state.getBlock().canSustainPlant(state, world, pos, getGrowthDirection(), getSimulatedPlantable());
    }

    @Nonnull
    public abstract IPlantable getSimulatedPlantable();

    @Nonnull
    public EnumFacing getGrowthDirection() { return EnumFacing.UP; }
}
