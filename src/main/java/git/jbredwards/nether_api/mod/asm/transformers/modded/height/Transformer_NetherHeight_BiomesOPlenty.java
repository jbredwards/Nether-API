/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.height;

import biomesoplenty.common.util.biome.GeneratorUtils;
import biomesoplenty.common.world.generator.GeneratorReplacing;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_BiomesOPlenty implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            // BOPHellBiome
            case "biomesoplenty.common.biome.nether.BOPHellBiome": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    /*
                     * getBlockPos:
                     * Old code:
                     * int localY = 127;
                     * ...
                     * for(roofOffset = 2; roofOffset <= roofDepth && localY + roofOffset <= 127; ++roofOffset)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * int localY = world.getActualHeight() - 1;
                     * ...
                     * for(roofOffset = 2; roofOffset <= roofDepth && localY + roofOffset <= world.getActualHeight() - 1; ++roofOffset)
                     * {
                     *     ...
                     * }
                     */
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "genTerrainBlocks" : "func_180622_a")) {
                        int changes = 0;
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 127) {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                                method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                                method.instructions.insertBefore(insn, new InsnNode(ISUB));
                                method.instructions.remove(insn);
                                if(++changes == 2) break methods;
                            }
                        }
                    }
                }

                // writes the changes
                @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // GeneratorUtils.ScatterYMethod
            case "biomesoplenty.common.util.biome.GeneratorUtils$ScatterYMethod": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    /*
                     * getBlockPos:
                     * Old code:
                     * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, 122), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirAbove().create());
                     * ...
                     * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, 122), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirBelow().create());
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, world.getActualHeight() - 6), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirAbove().create());
                     * ...
                     * pos = GeneratorUtils.getFirstBlockMatching(world, new BlockPos(x, GeneratorUtils.nextIntBetween(random, 1, world.getActualHeight() - 6), z), BlockQuery.buildAnd().add(BlockQueries.solid).withAirBelow().create());
                     */
                    if(method.name.equals("getBlockPos")) {
                        int changes = 0;
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 122) {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getActualHeight" : "func_72940_L", "()I", false));
                                method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 6));
                                method.instructions.insertBefore(insn, new InsnNode(ISUB));
                                method.instructions.remove(insn);
                                if(++changes == 2) break methods;
                            }
                        }
                    }
                }

                // writes the changes
                @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // BlockQuery.BlockPosQueryAltitude
            case "biomesoplenty.common.util.block.BlockQuery$BlockPosQueryAltitude": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    /*
                     * getBlockPos:
                     * Old code:
                     * return pos.getY() >= this.minHeight && pos.getY() <= this.maxHeight;
                     *
                     * New code:
                     * // Change max value based on nether height
                     * return pos.getY() >= this.minHeight && pos.getY() <= Hooks.maxHeight(this.maxHeight, world);
                     */
                    if(method.name.equals("matches")) {
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("maxHeight")) {
                                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/height/Transformer_NetherHeight_BiomesOPlenty$Hooks", "maxHeight", "(ILnet/minecraft/world/World;)I", false));
                                method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
                                break methods;
                            }
                        }
                    }
                }

                // writes the changes
                @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // GeneratorReplacing
            /*case "biomesoplenty.common.world.generator.GeneratorReplacing": {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                /*
                 * New code:
                 * // Run code twice if the nether height is twice what it normally would be
                 * @ASMGenerated
                 * public void scatter(World world, Random rand, BlockPos pos)
                 * {
                 *     Hooks.scatter(this, world, rand, pos, this.scatterYMethod);
                 * }
                 */
                /*@Nonnull final MethodNode method = new MethodNode(ACC_PUBLIC, "scatter", "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)V", null, null);
                @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ALOAD, 3);
                generator.visitVarInsn(ALOAD, 0);
                generator.visitFieldInsn(GETFIELD, "biomesoplenty/common/world/generator/GeneratorReplacing", "scatterYMethod", "Lbiomesoplenty/common/util/biome/GeneratorUtils$ScatterYMethod;");
                generator.visitMethodInsn(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/height/Transformer_NetherHeight_BiomesOPlenty$Hooks", "scatter", "(Lbiomesoplenty/common/world/generator/GeneratorReplacing;Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lbiomesoplenty/common/util/biome/GeneratorUtils$ScatterYMethod;)V", false);

                // writes the changes
                @Nonnull final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }*/
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static int maxHeight(final int maxHeight, @Nonnull final World world) { return world.provider.isNether() ? maxHeight + (world.getActualHeight() >> 8 << 7) : maxHeight; }
        public static void scatter(@Nonnull final GeneratorReplacing generator, @Nonnull final World world, @Nonnull final Random rand, @Nonnull final BlockPos pos, @Nonnull final GeneratorUtils.ScatterYMethod scatterYMethod) {
            final int amount;
            switch(scatterYMethod) {
                case NETHER_ROOF:
                case NETHER_SURFACE: amount = generator.getAmountToScatter(rand) << (world.getActualHeight() >> 8); break;
                default: amount = generator.getAmountToScatter(rand);
            }

            for(int i = 0; i < amount; i++) generator.generate(world, rand, generator.getScatterY(world, rand, pos.getX() + rand.nextInt(16) + 8, pos.getZ() + rand.nextInt(16) + 8));
        }
    }
}
