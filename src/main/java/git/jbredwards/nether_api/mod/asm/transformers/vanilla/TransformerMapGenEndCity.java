/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allow IEndBiome instances to specify whether they're an applicable biome
 * @author jbred
 *
 */
public final class TransformerMapGenEndCity implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, true, method -> method.name.equals(DEOBFUSCATED ? "canSpawnStructureAtCoords" : "func_75047_a"), (method, insn) -> {
            /*
             * canSpawnStructureAtCoords: (changes are around line 52)
             * Old code:
             * return i1 >= 60;
             *
             * New code:
             * // Only generate end cities within biomes that permit them
             * return Hooks.canBiomeGenerateEndCity(i1, this.endProvider, i, j) >= 60;
             */
            if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 60) {
                @Nonnull final InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/MapGenEndCity", DEOBFUSCATED ? "endProvider" : "field_186133_d", "Lnet/minecraft/world/gen/ChunkGeneratorEnd;"));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(genHookMethod("canBiomeGenerateEndCity", "(ILnet/minecraft/world/gen/ChunkGeneratorEnd;II)I"));
                method.instructions.insertBefore(insn, list);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static int canBiomeGenerateEndCity(final int islandHeight, @Nonnull final ChunkGeneratorEnd chunkGenerator, final int chunkX, final int chunkZ) {
            if(!(chunkGenerator instanceof INetherAPIChunkGenerator)) throw new IllegalStateException("The end chunk generator has been replaced! Please report this to Nether API's issue tracker.");
            @Nullable final Biome biome = ((INetherAPIChunkGenerator)chunkGenerator).getWorld().getBiomeProvider().getBiome(new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8));
            return biome instanceof IEndBiome ? (((IEndBiome)biome).generateEndCity((INetherAPIChunkGenerator)chunkGenerator, chunkX, chunkZ, islandHeight) ? 60 : 0) : islandHeight;
        }
    }
}
