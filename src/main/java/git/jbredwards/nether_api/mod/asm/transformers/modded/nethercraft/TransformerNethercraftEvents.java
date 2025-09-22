/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.nethercraft;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.entity.Entity;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Ensure all Nethercraft world generation is kept to within one biome
 * @author jbred
 *
 */
public final class TransformerNethercraftEvents implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        switch(transformedName) {
            case "com.legacy.nethercraft.entities.NetherEntityRegistry": {
                return transformMethod(basicClass, method -> method.name.equals("register") && method.desc.equals("(Ljava/lang/Class;III)V"), (method, insn) -> {
                    /*
                     * register:
                     * Old code:
                     * EntityRegistry.addSpawn(entityClass, weight, min, max, EnumCreatureType.MONSTER, new Biome[]{Biomes.HELL});
                     *
                     * New code:
                     * // Change default spawn biome for nethercraft mobs to the glowing grove
                     * EntityRegistry.addSpawn(entityClass, weight, min, max, EnumCreatureType.MONSTER, new Biome[]{NethercraftHandler.GLOWING_GROVE});
                     */
                    if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "HELL" : "field_76778_j")) {
                        ((FieldInsnNode)insn).owner = "git/jbredwards/nether_api/mod/common/compat/nethercraft/NethercraftHandler";
                        ((FieldInsnNode)insn).name = "GLOWING_GROVE";
                        ((FieldInsnNode)insn).desc = "Lgit/jbredwards/nether_api/mod/common/compat/nethercraft/BiomeNethercraft;";
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // remove ghost tree block updates
            case "com.legacy.nethercraft.world.NetherGenTree": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "generate" : "func_180709_b"), (method, insn) -> {
                    /*
                     * generate:
                     * Old code:
                     * if (i1 < 0 || i1 >= 128)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Use actual nether height instead of a hardcoded value
                     * if (i1 < 0 || i1 >= world.getActualHeight())
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                        method.instructions.insertBefore(insn, genHeightMethod());
                        method.instructions.remove(insn);
                    }
                    /*
                     * generate:
                     * Old code:
                     * world.setBlockState(mutablePos, BlocksNether.glowood_leaves.getDefaultState());
                     *
                     * New code:
                     * // Use better block flags
                     * world.setBlockState(mutablePos, BlocksNether.glowood_leaves.getDefaultState(), 18);
                     */
                    else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockState" : "func_175656_a")) {
                        method.instructions.insertBefore(insn, genBlockFlags());
                        if(!DEOBFUSCATED) ((MethodInsnNode)insn).name = "func_180501_a";
                        ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                    }

                    return BreakType.CONTINUE;
                });
            }

            // this mod rewrites the events in this class, remove old code
            case "com.legacy.nethercraft.world.NetherWorldEvent": {
                return transform(basicClass, classNode -> classNode.methods.removeIf(method -> method.name.equals("onGenerateLand") || method.name.equals("onNetherDecorated")));
            }

            // don't spawn entities on the client (nethercraft is so unfinished ISTG)
            case "com.legacy.nethercraft.entities.projectile.EntitySlimeEggs": {
                return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "onImpact" : "func_70184_a"), (method, insn) -> {
                    /*
                     * register:
                     * Old code:
                     * int slime;
                     * if (this.rand.nextInt(6) == 0)
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Don't spawn lava slime entities on the client
                     * int slime;
                     * if (Hooks.canSpawnSlime(this.rand, 6, this)))
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("nextInt")) {
                        ((JumpInsnNode)insn.getNext()).setOpcode(IFEQ);
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, genHookMethod("canSpawnSlime", "(Ljava/util/Random;ILnet/minecraft/entity/Entity;)Z"));
                        method.instructions.remove(insn);
                        return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // make more of the nethercraft entities fireproof by default
            case "com.legacy.nethercraft.entities.hostile.EntityBloodyZombie":
            case "com.legacy.nethercraft.entities.hostile.EntityDarkZombie":
            case "com.legacy.nethercraft.entities.hostile.EntityLavaSlime": {
                return transformMethod(basicClass, method -> method.name.equals("<init>"), (method, insn) -> {
                    /*
                     * Constructor:
                     * Old code:
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * //
                     * {
                     *     ...
                     *     this.isImmuneToFire = true;
                     * }
                     */
                    if(insn.getOpcode() == RETURN) {
                        method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                        method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/entity/Entity", DEOBFUSCATED ? "isImmuneToFire" : "field_70178_ae", "Z"));
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
        public static boolean canSpawnSlime(@Nonnull Random rand, int chance, @Nonnull Entity entity) {
            return !entity.world.isRemote && rand.nextInt(chance) == 0;
        }
    }
}
