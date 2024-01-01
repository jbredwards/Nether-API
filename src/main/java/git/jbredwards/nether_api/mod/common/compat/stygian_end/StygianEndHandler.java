/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.stygian_end;

import fluke.stygian.Stygian;
import fluke.stygian.block.ModBlocks;
import fluke.stygian.config.Configs;
import fluke.stygian.world.BiomeRegistrar;
import fluke.stygian.world.biomes.BiomeEndJungle;
import fluke.stygian.world.feature.WorldGenEnderCanopy;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorTheEnd;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public final class StygianEndHandler
{
    public static void registerBiomes(@Nonnull final INetherAPIRegistry registry) {
        // register stygian end unofficial biomes added via that mod's config
        try { registerBiomesUnofficial(registry); } // throws if using official version of the mod
        catch(final ReflectiveOperationException | IncompatibleClassChangeError ignored) {}

        // register base stygian end biomes
        registry.registerBiome(BiomeRegistrar.END_JUNGLE, NetherAPIConfig.StygianEnd.endJungleWeight);
        registry.registerBiome(BiomeRegistrar.END_VOLCANO, NetherAPIConfig.StygianEnd.endVolcanoWeight);
    }

    public static void init() {
        // don't cause block updates when generating ender canopies, fixes cascading gen issues & improves performance
        ((BiomeEndJungle)BiomeRegistrar.END_JUNGLE).endCanopyTree = new WorldGenEnderCanopy(false) {
            @Override
            public boolean generate(@Nonnull final World world, @Nonnull final Random rand, @Nonnull final BlockPos pos) {
                // only generate trees on sections that are large enough to support them
                if(world.getChunkProvider() instanceof ChunkProviderServer
                && ((ChunkProviderServer)world.getChunkProvider()).chunkGenerator instanceof ChunkGeneratorTheEnd
                && ((ChunkGeneratorTheEnd)((ChunkProviderServer)world.getChunkProvider()).chunkGenerator).getIslandHeightValue(pos.getX() >> 4, pos.getZ() >> 4, 1, 1) < 25)
                    return false;

                // if the height is good, or if client, or if not in the end: run generator
                return super.generate(world, rand, pos);
            }
        };
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        // leaves look awful while in the end due to the way the end sky renders, fix this by forcing them to render as fancy leaves
        ModBlocks.endLeaves.setGraphicsLevel(true);
    }

    static void registerBiomesUnofficial(@Nonnull final INetherAPIRegistry registry) throws ReflectiveOperationException, IncompatibleClassChangeError {
        if(Configs.worldgen.biomeIDs.length > Configs.worldgen.biomeWeights.length) throw new IllegalStateException("Found missing end biome weights in Stygian End Unofficial config!");
        for(int i = 0; i < Configs.worldgen.biomeIDs.length; i++) {
            @Nullable final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(Configs.worldgen.biomeIDs[i]));

            if(biome == null) Stygian.LOGGER.warn("Biome not found with ID: " + Configs.worldgen.biomeIDs[i] + ", skipping...");
            else registry.registerBiome(biome, Configs.worldgen.biomeWeights[i]);
        }
    }
}
