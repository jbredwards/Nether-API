/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allow IEndBiome instances to specify whether they're an applicable biome
 * @author jbred
 *
 */
public final class TransformerMapGenEndCity implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        if("net.minecraft.world.gen.structure.MapGenEndCity".equals(transformedName)) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, ClassReader.SKIP_FRAMES);
            methods:
            for(@Nonnull final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "canSpawnStructureAtCoords" : "func_75047_a")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
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
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/MapGenEndCity", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "endProvider" : "field_186133_d", "Lnet/minecraft/world/gen/ChunkGeneratorEnd;"));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerMapGenEndCity$Hooks", "canBiomeGenerateEndCity", "(ILnet/minecraft/world/gen/ChunkGeneratorEnd;II)I", false));
                            method.instructions.insertBefore(insn, list);
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
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
