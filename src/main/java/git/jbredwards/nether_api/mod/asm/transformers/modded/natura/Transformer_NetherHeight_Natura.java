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

package git.jbredwards.nether_api.mod.asm.transformers.modded.natura;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.google.common.collect.Sets;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_Natura implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        switch(transformedName) {
            // BloodwoodTreeGenerator
            case "com.progwml6.natura.world.worldgen.trees.nether.BloodwoodTreeGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("findCeiling"), (method, insn) -> {
                    /*
                     * findCeiling:
                     * Old code:
                     * while(height <= 120);
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * while(height <= world.getActualHeight());
                     */
                    if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 120) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // GlowshroomGenerator
            case "com.progwml6.natura.world.worldgen.GlowshroomGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("generateNether"), (method, insn) -> {
                    /*
                     * generateNether:
                     * Old code:
                     * if (BiomeDictionary.hasType(biome, Type.NETHER))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                     * if (Hooks.canGenerate(Hooks.VALID_GLOWSHROOM_BIOMES, biome))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn.getPrevious(), genHookField("VALID_GLOWSHROOM_BIOMES", "Ljava/util/Set;"));
                        method.instructions.insert(insn, genHookMethod("canGenerate", "(Ljava/util/Set;Lnet/minecraft/world/biome/Biome;)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = this.findGround(world, xSpawn, random.nextInt(64) + 32, zSpawn);
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = this.findGround(world, xSpawn, random.nextInt(64 + (world.getActualHeight() >> 8 << 7)) + 32, zSpawn);
                     */
                    else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 64) {
                        @Nonnull final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(genHeightOffset(true));
                        method.instructions.insert(insn, list);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = random.nextInt(128);
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = random.nextInt(world.getActualHeight());
                     */
                    else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 4));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.remove(insn);
                    }

                    return BreakType.CONTINUE;
                });
            }

            // NetherBerryBushGenerator
            case "com.progwml6.natura.world.worldgen.berry.nether.NetherBerryBushGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("generateBush"), (method, insn) -> {
                    /*
                     * generateBush:
                     * Old code:
                     * if (pos.getY() >= 0)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Account for modded nether biomes
                     * if (Hooks.canBerryGenerate(pos, world) >= 0)
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getY" : "func_177956_o")) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                        method.instructions.insertBefore(insn, genHookMethod("canBerryGenerate", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;)I"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // NetherMinableGenerator
            case "com.progwml6.natura.world.worldgen.NetherMinableGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("generateNether"), (method, insn) -> {
                    /*
                     * generateNether:
                     * Old code:
                     * if (this.shouldGenerateInDimension(world.provider.getDimension()) && BiomeDictionary.hasType(biome, Type.NETHER))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                     * if (this.shouldGenerateInDimension(world.provider.getDimension()) && Hooks.canGenerate(Hooks.VALID_MINABLE_BIOMES, biome))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn.getPrevious(), genHookField("VALID_MINABLE_BIOMES", "Ljava/util/Set;"));
                        method.instructions.insert(insn, genHookMethod("canGenerate", "(Ljava/util/Set;Lnet/minecraft/world/biome/Biome;)Z"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // NetherTreesGenerator
            case "com.progwml6.natura.world.worldgen.NetherTreesGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("generateNether"), (method, insn) -> {
                    /*
                     * generateNether:
                     * Old code:
                     * if (BiomeDictionary.hasType(biome, Type.NETHER))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                     * if (Hooks.canGenerate(Hooks.VALID_TREE_BIOMES, biome))
                     * {
                     *     ...
                     * }
                     */
                    if (insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn.getPrevious(), genHookField("VALID_TREE_BIOMES", "Ljava/util/Set;"));
                        method.instructions.insert(insn, genHookMethod("canGenerate", "(Ljava/util/Set;Lnet/minecraft/world/biome/Biome;)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = 72;
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = 72 + (world.getActualHeight() >> 8 << 7);
                     */
                    else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 72) {
                        @Nonnull final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(genHeightOffset(true));
                        method.instructions.insert(insn, list);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = random.nextInt(64) + 32;
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = random.nextInt(64 + (world.getActualHeight() >> 8 << 7)) + 32;
                     */
                    else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 64) {
                        @Nonnull final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(genHeightOffset(true));
                        method.instructions.insert(insn, list);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = random.nextInt(80) + 16;
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = random.nextInt(80 + (world.getActualHeight() >> 8 << 7)) + 16;
                     */
                    else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 80) {
                        @Nonnull final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(genHeightOffset(true));
                        method.instructions.insert(insn, list);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // VineGenerator
            case "com.progwml6.natura.world.worldgen.VineGenerator": {
                return transformMethod(basicClass, method -> method.name.equals("generateNether"), (method, insn) -> {
                    /*
                     * generateNether:
                     * Old code:
                     * if (BiomeDictionary.hasType(biome, Type.NETHER))
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Only generate features in vanilla hell biome by default, as they do not fit in many modded ones
                     * if (Hooks.canGenerate(Hooks.VALID_VINE_BIOMES, biome))
                     * {
                     *     ...
                     * }
                     */
                    if (insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals("hasType")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn.getPrevious(), genHookField("VALID_VINE_BIOMES", "Ljava/util/Set;"));
                        method.instructions.insert(insn, genHookMethod("canGenerate", "(Ljava/util/Set;Lnet/minecraft/world/biome/Biome;)Z"));
                        method.instructions.remove(insn);
                    }
                    /*
                     * generateNether:
                     * Old code:
                     * ySpawn = 108;
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * ySpawn = 108 + (world.getActualHeight() >> 8 << 7) + 5;
                     */
                    else if (insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 108) {
                        @Nonnull final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(genHeightOffset(true));
                        list.add(new InsnNode(ICONST_5));
                        list.add(new InsnNode(IADD));
                        method.instructions.insert(insn, list);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // Dynamic Trees mod compat:
            // BiomeDataBasePopulator
            case "maxhyper.dynamictreesnatura.worldgen.BiomeDataBasePopulator": {
                return transformMethod(basicClass, method -> method.name.equals("populate"), (method, insn) -> {
                    /*
                     * populate:
                     * Old code:
                     * this.jsonPopulator.populate(dbase)
                     *
                     * New code:
                     * // Inject all non-specified nether biomes into blacklist prior to saving gen data
                     * Hooks.populate(this.jsonPopulator, dbase)
                     */
                    if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("populate")) {
                        method.instructions.insert(insn, genHookMethod("populate", "(Lcom/ferreusveritas/dynamictrees/worldgen/BiomeDataBasePopulatorJson;Lcom/ferreusveritas/dynamictrees/worldgen/BiomeDataBase;)V"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static final Set<Biome> // exists to allow pack devs to add biomes (must be finalized before postInit)
                VALID_BERRY_BIOMES = Sets.newHashSet(Biomes.HELL),
                VALID_GLOWSHROOM_BIOMES = Sets.newHashSet(Biomes.HELL),
                VALID_MINABLE_BIOMES = Sets.newHashSet(Biomes.HELL),
                VALID_TREE_BIOMES = Sets.newHashSet(Biomes.HELL),
                VALID_VINE_BIOMES = Sets.newHashSet(Biomes.HELL);

        public static int canBerryGenerate(@Nonnull final BlockPos pos, @Nonnull final World world) {
            return pos.getY() >= 0 && canGenerate(VALID_BERRY_BIOMES, world.getBiome(pos)) ? pos.getY() : -1;
        }

        public static boolean canGenerate(@Nonnull final Set<Biome> validBiomes, @Nonnull final Biome biome) {
            return validBiomes.isEmpty() || validBiomes.contains(biome);
        }

        @Nonnull
        private static final Lock LOCK = new ReentrantLock();
        public static void populate(@Nonnull final BiomeDataBasePopulatorJson populator, @Nonnull final BiomeDataBase data) {
            if(VALID_TREE_BIOMES.isEmpty()) {
                populator.populate(data);
                return;
            }

            LOCK.lock();
            {
                @Nonnull final Set<Biome> blacklist = Sets.newHashSet(BiomeDataBasePopulatorJson.blacklistedBiomes);
                BiomeDataBasePopulatorJson.blacklistedBiomes.addAll(Sets.difference(BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER), VALID_TREE_BIOMES));

                populator.populate(data);
                BiomeDataBasePopulatorJson.blacklistedBiomes = blacklist;
            }
            LOCK.unlock();
        }
    }
}
