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
    public static int chance = 3, roomChance = 4, maxY = 80;

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
                final double y = MathHelper.getInt(rand, 10, maxY);
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
