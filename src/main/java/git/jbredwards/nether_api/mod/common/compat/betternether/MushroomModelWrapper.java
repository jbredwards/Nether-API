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

package git.jbredwards.nether_api.mod.common.compat.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBlockMushroom;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.DimensionType;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
class MushroomModelWrapper implements IBakedModel
{
    @Nonnull
    IBakedModel betterNetherModel, vanillaModel;
    MushroomModelWrapper(@Nonnull final ModelBakeEvent event, @Nonnull final ModelResourceLocation betterNetherModelIn, @Nonnull final ModelResourceLocation vanillaModelIn) {
        this(event.getModelManager().getMissingModel(), event.getModelRegistry().getObject(betterNetherModelIn), vanillaModelIn);
    }

    MushroomModelWrapper(@Nonnull final IBakedModel missingModel, @Nullable final IBakedModel betterNetherModelIn, @Nonnull final ModelResourceLocation vanillaModelIn) {
        betterNetherModel = betterNetherModelIn != null ? betterNetherModelIn : missingModel;

        try { vanillaModel = ModelLoaderRegistry.getModel(vanillaModelIn).bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()); }
        catch(@Nonnull final Exception e) { vanillaModel = betterNetherModel; }
    }
    
    static boolean useBetterNetherModel(@Nullable final IBlockState state) {
        switch(NetherAPIConfig.BetterNether.betterMushroomModels) {
            case Nether:
                if(!(state instanceof IExtendedBlockState) || ((IExtendedBlockState)state).getClean() == state) return false; // Only change in-world mushrooms.
                else if(Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.provider.getDimensionType() == DimensionType.NETHER) return true;
            case Biome: return state instanceof IExtendedBlockState && Boolean.TRUE.equals(((IExtendedBlockState)state).getValue(TransformerBlockMushroom.Hooks.IS_BIOME_3D));
            case Never: return false;
        }

        return true;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
        return useBetterNetherModel(state) ? betterNetherModel.getQuads(state, side, rand) : vanillaModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion(@Nonnull final IBlockState state) {
        return useBetterNetherModel(state) ? betterNetherModel.isAmbientOcclusion(state) : vanillaModel.isAmbientOcclusion(state);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return useBetterNetherModel(null) ? betterNetherModel.isAmbientOcclusion() : vanillaModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return useBetterNetherModel(null) ? betterNetherModel.isGui3d() : vanillaModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return useBetterNetherModel(null) ? betterNetherModel.isBuiltInRenderer() : vanillaModel.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return useBetterNetherModel(null) ? betterNetherModel.getParticleTexture() : vanillaModel.getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return useBetterNetherModel(null) ? betterNetherModel.getItemCameraTransforms() : vanillaModel.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return useBetterNetherModel(null) ? betterNetherModel.getOverrides() : vanillaModel.getOverrides();
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
        return useBetterNetherModel(null) ? betterNetherModel.handlePerspective(cameraTransformType) : vanillaModel.handlePerspective(cameraTransformType);
    }
}
