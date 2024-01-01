/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world.gen;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCavesHell;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class MapGenCavesEnd extends MapGenCavesHell
{
    public static boolean generateOnStartIsland = false;
    public static int chance = 3, roomChance = 4;

    @Override
    public void generate(@Nonnull World worldIn, int x, int z, @Nonnull ChunkPrimer primer) {
        if(generateOnStartIsland || (long)x * (long)x + (long)z * (long)z > 2048L) super.generate(worldIn, x, z, primer);
    }

    @Override
    protected void recursiveGenerate(@Nonnull World worldIn, int chunkX, int chunkZ, int originalX, int originalZ, @Nonnull ChunkPrimer chunkPrimerIn) {
        if(rand.nextInt(chance) == 0) {
            final int max = rand.nextInt(rand.nextInt(rand.nextInt(10) + 1) + 1);
            for(int i = 0; i < max; i++) {
                final double x = (chunkX << 4) + rand.nextInt(16);
                final double y = MathHelper.getInt(rand, 10, 80);
                final double z = (chunkZ << 4) + rand.nextInt(16);
                int tunnels = 1;
                
                if(rand.nextInt(roomChance) == 0) {
                    addRoom(rand.nextLong(), originalX, originalZ, chunkPrimerIn, x, y, z);
                    tunnels += rand.nextInt(4);
                }
                
                for(int j = 0; j < tunnels; j++) {
                    final float radius = rand.nextFloat() * 6;
                    final float direction = rand.nextFloat() * (float)Math.PI * 2;
                    final float length = (rand.nextFloat() - 0.5f) / 4;

                    addTunnel(rand.nextLong(), originalX, originalZ, chunkPrimerIn, x, y, z, radius, direction, length, 0, 0, 0.5);
                }
            }
        }
    }
}
