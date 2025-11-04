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

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty;
import git.jbredwards.nether_api.api.util.PlantUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public abstract class AbstractBlockRooty extends BlockRooty
{
    @Nonnull
    private static final BlockPos[] searchPattern = ReflectionHelper.getPrivateValue(MimicProperty.class, null, "searchPattern");
    protected AbstractBlockRooty(@Nonnull final String name, @Nonnull final Material material, final boolean isTileEntity) {
        super(name, material, isTileEntity);
    }

    @Nonnull
    public abstract EnumPlantType getSoilType();

    @Nonnull
    protected abstract Block getFallbackMimic();

    @Nonnull
    protected IBlockState getFallbackMimic(@Nonnull final IBlockAccess access, @Nonnull final BlockPos pos) {
        return getFallbackMimic().getDefaultState();
    }

    @Nonnull
    @Override
    public IBlockState getMimic(@Nonnull final IBlockAccess access, @Nonnull final BlockPos pos) {
        @Nonnull final IPlantable plantable = PlantUtils.createPlantable(getSoilType());
        for(@Nonnull final BlockPos toAdd : searchPattern) {
            @Nonnull final BlockPos offset = pos.add(toAdd);
            @Nonnull final IBlockState state = access.getBlockState(offset);

            if(!state.getBlock().hasTileEntity(state) && !(state.getBlock() instanceof MimicProperty.IMimic)
            && state.getBlock().canSustainPlant(state, access, offset, EnumFacing.UP, plantable)) return state;
        }

        return getFallbackMimic(access, pos);
    }

    @Nonnull
    @Override
    public IBlockState getDecayBlockState(@Nonnull final IBlockAccess access, @Nonnull final BlockPos pos) {
        return getMimic(access, pos);
    }

    @Nonnull
    @Override
    public Item getItemDropped(@Nonnull final IBlockState state, @Nonnull final Random rand, final int fortune) {
        return Item.getItemFromBlock(getFallbackMimic());
    }

    @Override
    public boolean canRenderInLayer(@Nonnull final IBlockState state, @Nonnull final BlockRenderLayer layer) {
        return true; // Handled via baked model.
    }

    @Override
    public boolean canSustainPlant(@Nonnull final IBlockState state, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
        if(super.canSustainPlant(state, world, pos, direction, plantable)) return true;

        @Nonnull final IBlockState mimic = getMimic(world, pos);
        return mimic.getBlock().canSustainPlant(mimic, world, pos, direction, plantable);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull final IBlockState state, @Nonnull final RayTraceResult target, @Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final EntityPlayer player) {
        @Nonnull final IBlockState mimic = getMimic(world, pos);
        return mimic.getBlock().getPickBlock(mimic, target, world, pos, player);
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        @Nonnull final IBlockState mimic = getMimic(worldIn, pos);
        return mimic.getBlock().getItem(worldIn, pos, mimic);
    }

    @Nonnull
    @Override
    public SoundType getSoundType(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos, @Nullable final Entity entity) {
        @Nonnull final IBlockState mimic = getMimic(world, pos);
        return mimic.getBlock().getSoundType(mimic, world, pos, entity);
    }

    @Nonnull
    @Override
    public SoundType getSoundType() { return getFallbackMimic().getSoundType(); }
}
