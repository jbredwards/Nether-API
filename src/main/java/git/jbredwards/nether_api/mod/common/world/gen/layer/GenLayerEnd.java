/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen.layer;

import git.jbredwards.nether_api.mod.common.world.biome.BiomeProviderTheEnd;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 *
 * @author jbred
 *
 */
public class GenLayerEnd extends GenLayer
{
    protected static final int END_ID = Biome.getIdForBiome(Biomes.SKY);
    protected static final int VOID_ID = Biome.getIdForBiome(Biomes.VOID);

    @Nonnull
    protected final BiomeProviderTheEnd biomeProvider;
    public GenLayerEnd(@Nonnull BiomeProviderTheEnd biomeProviderIn, @Nonnull GenLayer parentIn) {
        super(1);
        parent = parentIn;
        biomeProvider = biomeProviderIn;
    }

    @Nonnull
    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        final int[] out = IntCache.getIntCache(areaWidth * areaHeight);
        // only use vanilla end biome at starting main end region
        if((long)(areaX >> 4) * (long)(areaX >> 4) + (long)(areaZ >> 4) * (long)(areaZ >> 4) <= 2048L) Arrays.fill(out, END_ID);
        // when far enough from the starting region, allow for custom end biomes and void biome
        else { // System.arraycopy(parent.getInts(areaX, areaZ, areaWidth, areaHeight), 0, out, 0, out.length);
            final int[] biomeIds = parent.getInts(areaX, areaZ, areaWidth, areaHeight);
            for(int x = 0; x < areaWidth; x++) {
                final int blockX = areaX + x;
                for(int z = 0; z < areaHeight; z++) {
                    final int index = x + z * areaHeight;
                    /*if(isVoidAt(blockX, areaZ + z)) out[index] = VOID_ID;
                    else*/ out[index] = biomeIds[index];
                }
            }
        }

        return out;
    }

    protected boolean isVoidAt(int blockX, int blockZ) {
        final double vanillaBlockX = blockX / 8.0 + 1;
        final double vanillaBlockZ = blockX / 8.0 + 1;
        final double chunkX = blockX / 16.0;
        final double chunkZ = blockZ / 16.0;

        double height = 100 - Math.sqrt(vanillaBlockX * vanillaBlockX + vanillaBlockZ * vanillaBlockZ) * 8;
        for(int x = -12; x <= 12; x++) {
            final double offsetX = chunkX + x;
            for(int z = -12; z <= 12; z++) {
                final double offsetZ = chunkZ + z;
                if(offsetX * offsetX + offsetZ * offsetZ > 2048 && biomeProvider.islandNoise.getValue(offsetX, offsetZ) < -0.9) {
                    final double scale = (Math.abs(offsetX) * 3439 + Math.abs(offsetZ) * 147) % 13 + 9;
                    final double fracX = 1 - (x << 1), fracZ = 1 - (z << 1);
                    final double newHeight = 100 - Math.sqrt(fracX * fracX + fracZ * fracZ) * scale;
                    if(height < newHeight) height = newHeight;
                }
            }
        }

        return height < -20;
    }
}
