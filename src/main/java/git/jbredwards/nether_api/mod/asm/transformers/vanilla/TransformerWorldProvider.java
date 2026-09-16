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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.objectweb.asm.commons.GeneratorAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fix player spawn locations in other dimensions
 * @author jbred
 *
 */
public final class TransformerWorldProvider implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            if(!transformedName.equals("net.minecraft.world.WorldProvider")) {
                classNode.methods.removeIf(method -> method.name.equals(DEOBFUSCATED ? "canCoordinateBeSpawn" : "func_76566_a"));
                return;
            }

            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "canCoordinateBeSpawn" : "func_76566_a"), (method, insn) -> {
                // remove existing body data
                method.instructions.clear();
                if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
                if(method.localVariables != null) method.localVariables.clear();
                if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
                if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

                // write new body data
                @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.visitVarInsn(ALOAD, 0);
                generator.visitFieldInsn(GETFIELD, classNode.name, DEOBFUSCATED ? "world" : "field_76579_a", "Lnet/minecraft/world/World;");
                generator.visitVarInsn(ILOAD, 1);
                generator.visitVarInsn(ILOAD, 2);
                generator.visitMethodInsn(INVOKESTATIC, genHookClass(), "canCoordinateBeSpawn", "(Lnet/minecraft/world/World;II)Z", false);
                generator.visitInsn(IRETURN);
                return BreakType.METHODS;
            });

            transformMethod(classNode, method -> method.name.equals("getRandomizedSpawnPoint"), (method, insn) -> {
                // remove existing body data
                method.instructions.clear();
                if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
                if(method.localVariables != null) method.localVariables.clear();
                if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
                if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

                // write new body data
                @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.visitVarInsn(ALOAD, 0);
                generator.visitFieldInsn(GETFIELD, classNode.name, DEOBFUSCATED ? "world" : "field_76579_a", "Lnet/minecraft/world/World;");
                generator.visitMethodInsn(INVOKESTATIC, genHookClass(), "getRandomizedSpawnPoint", "(Lnet/minecraft/world/World;)Lnet/minecraft/util/math/BlockPos;", false);
                generator.visitInsn(ARETURN);
                return BreakType.METHODS;
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canCoordinateBeSpawn(@Nonnull final World world, final int x, final int z) {
            @Nonnull final Chunk chunk = world.getChunk(x >> 4, z >> 4);
            if(chunk.getHeightValue(x & 15, z & 15) == 0) return false; // Never spawn players into the void.

            @Nonnull final Biome biome = world.getBiome(new BlockPos(x, 0, z));
            if(biome.ignorePlayerSpawnSuitability()) return true;
            else if(world.provider.canRespawnHere()) { // Don't change overworld spawn logic.
                return world.getGroundAboveSeaLevel(new BlockPos(x, 0, z)).getBlock() == Blocks.GRASS;
            }

            // Instead of using world.getGroundAboveSeaLevel(pos), also check for fluids and block "spawn-ability".
            @Nullable final BlockPos spawnCoord = world.provider.getSpawnCoordinate();
            @Nonnull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, spawnCoord != null ? spawnCoord.getY() : world.getSeaLevel(), z);

            while(pos.getY() < world.getActualHeight() - 1 && !isBlockGround(world.getMinecraftServer(), chunk, pos.move(EnumFacing.UP)));
            return pos.getY() < world.getActualHeight() - 1 && isBlockGround(world.getMinecraftServer(), chunk, pos);
        }

        @Nonnull
        public static BlockPos getRandomizedSpawnPoint(@Nonnull final World world) {
            if(NetherAPI.isPerfectSpawnLoaded) return world.getSpawnPoint();

            @Nullable final BlockPos spawnCoord = world.provider.getSpawnCoordinate();
            @Nonnull final BlockPos spawnPoint;

            if(spawnCoord != null) spawnPoint = spawnCoord;
            else spawnPoint = world.provider.isNether() ? world.provider.getSpawnPoint() : world.getSpawnPoint();

            if(world.getWorldInfo().getGameType() == GameType.ADVENTURE) return spawnPoint;
            int spawnFuzz = world instanceof WorldServer ? world.getWorldInfo().getTerrainType().getSpawnFuzz((WorldServer)world, world.getMinecraftServer()) : 1;
            spawnFuzz = Math.min(spawnFuzz, MathHelper.floor(world.getWorldBorder().getClosestDistance(spawnPoint.getX(), spawnPoint.getZ())));

            if(spawnFuzz > 0) {
                if(spawnFuzz < 2) spawnFuzz = 2;
                final int spawnFuzzHalf = spawnFuzz >> 1;
                if(world.provider.canRespawnHere()) { // Don't change overworld spawn logic.
                    return world.provider.isNether() ? spawnPoint : world.getTopSolidOrLiquidBlock(spawnPoint
                            .add(spawnFuzzHalf - world.rand.nextInt(spawnFuzz), 0, spawnFuzzHalf - world.rand.nextInt(spawnFuzz)));
                }

                final int y = spawnCoord != null ? spawnCoord.getY() : world.provider.isNether() ? 32 : world.getSeaLevel();
                final int spawnAttempts = 1000; // Same # of spawn attempts as initial spawn point set.
                @Nonnull final IntSet checkedPositions = new IntOpenHashSet(MathHelper.clamp(spawnFuzz * spawnFuzz, 1, spawnAttempts));
                for(int i = 0; i < spawnAttempts; i++) {
                    int x = world.rand.nextInt(spawnFuzz);
                    int z = world.rand.nextInt(spawnFuzz);

                    if(checkedPositions.contains(x * spawnFuzz + z)) continue;
                    else checkedPositions.add(x * spawnFuzz + z);
                    x += spawnPoint.getX() - spawnFuzzHalf;
                    z += spawnPoint.getZ() - spawnFuzzHalf;

                    if(world.provider.canCoordinateBeSpawn(x, z)) {
                        @Nonnull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
                        @Nonnull final Chunk chunk = world.getChunk(pos);
                        while(pos.getY() < world.getActualHeight() && !isBlockGround(world.getMinecraftServer(), chunk, pos.move(EnumFacing.UP)));
                        return pos.move(EnumFacing.DOWN, 2).toImmutable();
                    }
                }
            }

            return spawnPoint;
        }

        // Helper.
        private static boolean isBlockGround(@Nullable final MinecraftServer server, @Nonnull final Chunk chunk, @Nonnull final BlockPos.MutableBlockPos pos) {
            @Nonnull final IBlockState state = chunk.getBlockState(pos);
            if(!state.getMaterial().blocksMovement() || state.getMaterial().isLiquid() || state.getBlock().isAir(state, chunk.getWorld(), pos)
            || server != null && !state.canEntitySpawn(FakePlayerFactory.getMinecraft(server.getWorld(0)))) return false;

            @Nonnull final IBlockState above = chunk.getBlockState(pos.move(EnumFacing.UP));
            pos.move(EnumFacing.DOWN);
            return !above.causesSuffocation() && !state.getMaterial().isLiquid() && state.getMaterial() != Material.FIRE;
        }
    }
}
