/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.compat.stygain_end;

import fluke.stygian.Stygian;
import fluke.stygian.config.Configs;
import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class StygainEndHandler
{
    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {
        // using stygain end unofficial, preserve config settings
        try { registerBiomesUnofficial(registry); }
        // using official stygain end version, revert to fallback configs
        catch(final ReflectiveOperationException e) {
            // TODO
        }
    }

    static void registerBiomesUnofficial(@Nonnull INetherAPIRegistry registry) throws ReflectiveOperationException {
        if(Configs.worldgen.biomeIDs.length > Configs.worldgen.biomeWeights.length) throw new IllegalStateException("Found missing end biome weights in Stygain End Unofficial config!");
        for(int i = 0; i < Configs.worldgen.biomeIDs.length; i++) {
            final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(Configs.worldgen.biomeIDs[i]));

            if(biome == null) Stygian.LOGGER.warn("Biome not found with ID: " + Configs.worldgen.biomeIDs[i] + ", skipping...");
            else if(biome != Biomes.SKY) registry.registerBiome(biome, Configs.worldgen.biomeWeights[i]);
        }
    }
}
