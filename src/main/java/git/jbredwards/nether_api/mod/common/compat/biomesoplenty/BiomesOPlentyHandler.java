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

package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.biome.IExtendedBiome;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.enums.BOPClimates;
import biomesoplenty.common.block.BlockBOPGrass;
import biomesoplenty.common.world.WorldTypeBOP;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.util.NetherAPIProperties;
import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class BiomesOPlentyHandler
{
    /**
     * Consistent behavior with BOP, but with the config option to override it.
     */
    public static boolean allowBOPNetherBiomes(@Nonnull World world) {
        return !NetherAPIConfig.BOP.dependentBOPHellBiomes || world.getWorldType() instanceof WorldTypeBOP;
    }

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry, @Nonnull World world) {
        if(allowBOPNetherBiomes(world)) BOPBiomes.REG_INSTANCE.getPresentBiomes().forEach(biome -> {
            final IExtendedBiome extended = BOPBiomes.REG_INSTANCE.getExtendedBiome(biome);
            if(extended != null) registry.registerBiome(biome, extended.getWeightMap().getOrDefault(BOPClimates.HELL, 0));
        });
    }

    private static boolean canSurvive(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumPlantType type) {
        @Nonnull final IBlockState state = world.getBlockState(pos);
        return state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, PlantUtils.createPlantable(type));
    }

    /**
     * Fix BOP block quarries.
     */
    private static void initQuarries() {
        @Nonnull final IBlockPosQuery fertileOrNetherrack = BlockQueries.fertileOrNetherrack;
        BlockQueries.fertileOrNetherrack = (world, pos) -> fertileOrNetherrack.matches(world, pos) || canSurvive(world, pos, PlantUtils.NETHERRACK_PLANT_TYPE);

        @Nonnull final IBlockPosQuery sustainsNether = BlockQueries.sustainsNether;
        BlockQueries.sustainsNether = (world, pos) -> sustainsNether.matches(world, pos) || canSurvive(world, pos, PlantUtils.SOUL_SAND_PLANT_TYPE);

        @Nonnull final IBlockPosQuery endish = BlockQueries.endish;
        BlockQueries.endish = (world, pos) -> endish.matches(world, pos) || canSurvive(world, pos, PlantUtils.END_PLANT_TYPE);

        @Nonnull final IBlockPosQuery hellish = BlockQueries.hellish;
        BlockQueries.hellish = (world, pos) -> hellish.matches(world, pos) || canSurvive(world, pos, PlantUtils.NETHERRACK_PLANT_TYPE);
    }

    public static void init() {
        initQuarries();

        // Add support for NetherEx path creation.
        NetherAPIProperties.registerNetherExPathable(
                BOPBlocks.grass.getDefaultState().withProperty(BlockBOPGrass.VARIANT, BlockBOPGrass.BOPGrassType.MYCELIAL_NETHERRACK),
                BOPBlocks.grass.getDefaultState().withProperty(BlockBOPGrass.VARIANT, BlockBOPGrass.BOPGrassType.OVERGROWN_NETHERRACK));
    }
}
