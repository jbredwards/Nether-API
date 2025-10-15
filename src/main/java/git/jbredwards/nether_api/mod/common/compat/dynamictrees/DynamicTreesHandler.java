/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty;
import com.google.common.collect.ImmutableList;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootyEndStone;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootyNetherrack;
import git.jbredwards.nether_api.mod.common.compat.dynamictrees.block.BlockRootySoulSand;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class DynamicTreesHandler
{
    @SubscribeEvent
    static void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BlockRootyEndStone.INSTANCE, BlockRootyNetherrack.INSTANCE, BlockRootySoulSand.INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerColors(@Nonnull final ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if(world == null || pos == null) return -1;
            else if(tintIndex == 1) return ((BlockRooty)state.getBlock()).rootColor(state, world, pos);

            @Nonnull final IBlockState mimic = ((BlockRooty)state.getBlock()).getMimic(world, pos);
            return event.getBlockColors().colorMultiplier(mimic, world, pos, Math.max(0, tintIndex - 2));
        }, BlockRootyEndStone.INSTANCE, BlockRootyNetherrack.INSTANCE, BlockRootySoulSand.INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerMappers(@Nonnull final ModelRegistryEvent event) {
        ModelLoader.setCustomStateMapper(BlockRootyEndStone.INSTANCE, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
        ModelLoader.setCustomStateMapper(BlockRootyNetherrack.INSTANCE, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
        ModelLoader.setCustomStateMapper(BlockRootySoulSand.INSTANCE, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModels(@Nonnull final ModelBakeEvent event) {
        registerModel(event.getModelRegistry(), event.getModelManager().getBlockModelShapes(), BlockRootyEndStone.INSTANCE);
        registerModel(event.getModelRegistry(), event.getModelManager().getBlockModelShapes(), BlockRootyNetherrack.INSTANCE);
        registerModel(event.getModelRegistry(), event.getModelManager().getBlockModelShapes(), BlockRootySoulSand.INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModel(@Nonnull final IRegistry<ModelResourceLocation, IBakedModel> registry, @Nonnull final BlockModelShapes shapes, @Nonnull final BlockRooty root) {
        shapes.getBlockStateMapper().getVariants(root).values().forEach(location -> registry.putObject(location, new BakedModelWrapper<IBakedModel>(shapes.getModelManager().getModel(location)) {
            @Nonnull
            @Override
            public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
                if(state instanceof IExtendedBlockState) {
                    @Nonnull final IBlockState mimic = ((IExtendedBlockState)state).getValue(MimicProperty.MIMIC);
                    @Nonnull final ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

                    @Nullable final BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
                    if(layer == null || mimic.getBlock().canRenderInLayer(mimic, layer)) {
                        @Nonnull final IBakedModel mimicModel = shapes.getModelForState(mimic);
                        quads.add(mimicModel.getQuads(mimic, side, rand).stream().map(quad -> quad.hasTintIndex() ? offsetTintIndex(quad) : quad).toArray(BakedQuad[]::new));
                    }

                    if(layer == null || layer == BlockRenderLayer.CUTOUT_MIPPED) quads.addAll(originalModel.getQuads(state, side, rand));
                    return quads.build();
                }

                return originalModel.getQuads(state, side, rand);
            }

            @Nonnull
            BakedQuad offsetTintIndex(@Nonnull final BakedQuad quad) {
                return new BakedQuad(quad.getVertexData(), quad.getTintIndex() + 2, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
            }
        }));
    }
}
