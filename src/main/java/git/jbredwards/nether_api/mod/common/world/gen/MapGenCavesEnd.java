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

import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
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
    @Override
    public void generate(@Nonnull final World worldIn, final int x, final int z, @Nonnull final ChunkPrimer primer) {
        if(NetherAPIConfig.endCavesOnStartIsland || (long)x * (long)x + (long)z * (long)z > 2048L) super.generate(worldIn, x, z, primer);
    }

    @Override
    protected void recursiveGenerate(@Nonnull final World worldIn, final int chunkX, final int chunkZ, final int originalX, final int originalZ, @Nonnull final ChunkPrimer chunkPrimerIn) {
        if(rand.nextInt(NetherAPIConfig.advanced.endCaveChance) == 0) {
            final int maxCfg = MathHelper.getInt(rand, NetherAPIConfig.advanced.endCaveMin, NetherAPIConfig.advanced.endCaveMax);
            final int max = rand.nextInt(rand.nextInt(maxCfg + 1) + 1);
            for(int i = 0; i < max; i++) {
                final double x = (chunkX << 4) + rand.nextInt(16);
                final double y = MathHelper.getInt(rand, NetherAPIConfig.advanced.endCaveMinY, NetherAPIConfig.advanced.endCaveMaxY);
                final double z = (chunkZ << 4) + rand.nextInt(16);
                int tunnels = 1;
                
                if(rand.nextInt(NetherAPIConfig.advanced.endCaveRoomChance) == 0) {
                    addRoom(rand.nextLong(), originalX, originalZ, chunkPrimerIn, x, y, z);
                    tunnels += MathHelper.getInt(rand, NetherAPIConfig.advanced.endCaveRoomTunnelsMin, NetherAPIConfig.advanced.endCaveRoomTunnelsMax);
                }
                
                for(int j = 0; j < tunnels; j++) {
                    final float radius = MathHelper.nextFloat(rand, NetherAPIConfig.advanced.endCaveTunnelMinRadius, NetherAPIConfig.advanced.endCaveTunnelMaxRadius);
                    final float heightMul = MathHelper.nextFloat(rand, NetherAPIConfig.advanced.endCaveTunnelMinHeightMul, NetherAPIConfig.advanced.endCaveTunnelMaxHeightMul);

                    final float rotXZ = rand.nextFloat() * (float)Math.PI * 2;
                    final float rotY = MathHelper.nextFloat(rand, -NetherAPIConfig.advanced.endCaveRotYMul, NetherAPIConfig.advanced.endCaveRotYMul);

                    addTunnel(rand.nextLong(), originalX, originalZ, chunkPrimerIn, x, y, z, radius, rotXZ, rotY, 0, 0, heightMul);
                }
            }
        }
    }

    @Override
    protected void addRoom(final long seed, final int originalX, final int originalZ, @Nonnull final ChunkPrimer primer, final double x, final double y, final double z) {
        addTunnel(seed, originalX, originalZ, primer, x, y, z,
                MathHelper.nextFloat(rand, NetherAPIConfig.advanced.endCaveRoomMinRadius, NetherAPIConfig.advanced.endCaveRoomMaxRadius),
                0, 0, -1, -1,
                MathHelper.nextFloat(rand, NetherAPIConfig.advanced.endCaveRoomMinHeightMul, NetherAPIConfig.advanced.endCaveRoomMaxHeightMul));
    }
}
