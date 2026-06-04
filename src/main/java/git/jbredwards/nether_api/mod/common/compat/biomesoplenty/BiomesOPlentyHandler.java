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
import biomesoplenty.common.block.BlockBOPDoublePlant;
import biomesoplenty.common.block.BlockColoring;
import biomesoplenty.common.world.WorldTypeBOP;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
                -> tintIndex != 1 ? -1
                : BlockColoring.GRASS_COLORING.colorMultiplier(state, world, pos, tintIndex), BOPBlocks.grass);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex)
                -> tintIndex != 1 && state.getValue(BlockBOPDoublePlant.VARIANT) != BlockBOPDoublePlant.DoublePlantType.FLAX ? -1
                : ((BlockBOPDoublePlant)BOPBlocks.double_plant).getBlockColor().colorMultiplier(state, world, pos, tintIndex), BOPBlocks.double_plant);
    }

    /**
     * Fix BOP grass block color tint indexes.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModels(@Nonnull final ModelBakeEvent event) {
        registerModel(event.getModelRegistry(), event.getModelManager(), BOPBlocks.grass, state -> true);
        registerModel(event.getModelRegistry(), event.getModelManager(), BOPBlocks.double_plant, state -> state.getValue(BlockBOPDoublePlant.VARIANT) != BlockBOPDoublePlant.DoublePlantType.FLAX);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModel(@Nonnull final IRegistry<ModelResourceLocation, IBakedModel> registry, @Nonnull final ModelManager manager, @Nonnull final Block block, @Nonnull final Predicate<IBlockState> condition) {
        manager.getBlockModelShapes().getBlockStateMapper().getVariants(block).entrySet().stream().filter(e -> condition.test(e.getKey())).map(Map.Entry::getValue).forEach(location -> {
            registry.putObject(location, new BakedModelWrapper<IBakedModel>(manager.getModel(location)) {
                @Nonnull
                @Override
                public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
                    @Nonnull final List<BakedQuad> quads = super.getQuads(state, side, rand);
                    for(@Nonnull final BakedQuad quad : quads) if(quad.hasTintIndex()) quad.tintIndex = 1;
                    return quads;
                }

                @Nonnull
                @Override
                public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
                    return Pair.of(this, super.handlePerspective(cameraTransformType).getRight());
                }
            });
        });
    }
}
