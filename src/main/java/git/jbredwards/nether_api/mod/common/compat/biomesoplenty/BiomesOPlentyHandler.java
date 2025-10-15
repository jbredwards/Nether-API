/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.biomesoplenty;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.biome.IExtendedBiome;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.enums.BOPClimates;
import biomesoplenty.common.block.BlockColoring;
import biomesoplenty.common.world.WorldTypeBOP;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public static void init() {
        @Nonnull final IBlockPosQuery fertileOrNetherrack = BlockQueries.fertileOrNetherrack;
        BlockQueries.fertileOrNetherrack = (world, pos) -> fertileOrNetherrack.matches(world, pos) || canSurvive(world, pos, PlantUtils.NETHERRACK_PLANT_TYPE);

        @Nonnull final IBlockPosQuery sustainsNether = BlockQueries.sustainsNether;
        BlockQueries.sustainsNether = (world, pos) -> sustainsNether.matches(world, pos) || canSurvive(world, pos, PlantUtils.SOUL_SAND_PLANT_TYPE);

        @Nonnull final IBlockPosQuery endish = BlockQueries.endish;
        BlockQueries.endish = (world, pos) -> endish.matches(world, pos) || canSurvive(world, pos, PlantUtils.END_PLANT_TYPE);

        @Nonnull final IBlockPosQuery hellish = BlockQueries.hellish;
        BlockQueries.hellish = (world, pos) -> hellish.matches(world, pos) || canSurvive(world, pos, PlantUtils.NETHERRACK_PLANT_TYPE);
    }

    /**
     * Fix BOP grass block particle colors.
     */
    @SideOnly(Side.CLIENT)
    public static void initClient() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex)
                -> tintIndex == 0 && MinecraftForgeClient.getRenderLayer() == null ? -1
                : BlockColoring.GRASS_COLORING.colorMultiplier(state, world, pos, tintIndex), BOPBlocks.grass);
    }
}
