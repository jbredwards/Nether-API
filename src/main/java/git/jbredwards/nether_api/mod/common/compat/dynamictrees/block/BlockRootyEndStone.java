/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.dynamictrees.block;

import git.jbredwards.nether_api.api.util.PlantUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.EnumPlantType;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class BlockRootyEndStone extends AbstractBlockRooty
{
    @Nonnull
    public static final BlockRootyEndStone INSTANCE = new BlockRootyEndStone();
    private BlockRootyEndStone() { super("rooty_endstone", Material.ROCK, false); }

    @Nonnull
    @Override
    public EnumPlantType getSoilType() { return PlantUtils.END_PLANT_TYPE; }

    @Nonnull
    @Override
    protected Block getFallbackMimic() { return Blocks.END_STONE; }
}
