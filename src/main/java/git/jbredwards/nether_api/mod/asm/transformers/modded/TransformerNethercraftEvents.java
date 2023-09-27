/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Ensure all Nethercraft world generation is kept to within one biome
 * @author jbred
 *
 */
public final class TransformerNethercraftEvents implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        switch(transformedName) {
            case "com.legacy.nethercraft.entities.NetherEntityRegistry": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
                    /*
                     * register:
                     * Old code:
                     * EntityRegistry.addSpawn(entityClass, weight, min, max, EnumCreatureType.MONSTER, new Biome[]{Biomes.HELL});
                     *
                     * New code:
                     * // Change default spawn biome for nethercraft mobs to the glowing grove
                     * EntityRegistry.addSpawn(entityClass, weight, min, max, EnumCreatureType.MONSTER, new Biome[]{NethercraftHandler.GLOWING_GROVE});
                     */
                    if(method.name.equals("register") && method.desc.equals("(Ljava/lang/Class;III)V")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode) insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "HELL" : "field_76778_j")) {
                                ((FieldInsnNode)insn).owner = "git/jbredwards/nether_api/mod/common/compat/nethercraft/NethercraftHandler";
                                ((FieldInsnNode)insn).name = "GLOWING_GROVE";
                                ((FieldInsnNode)insn).desc = "Lgit/jbredwards/nether_api/mod/common/compat/nethercraft/BiomeNethercraft;";
                                break methods;
                            }
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // remove ghost tree block updates
            case "com.legacy.nethercraft.world.NetherGenTree": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                for(final MethodNode method : classNode.methods) {
                    /*
                     * generate:
                     * Old code:
                     * world.setBlockState(mutablePos, BlocksNether.glowood_leaves.getDefaultState());
                     *
                     * New code:
                     * // Use better block flags
                     * world.setBlockState(mutablePos, BlocksNether.glowood_leaves.getDefaultState(), 18);
                     */
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "generate" : "func_180709_b")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_175656_a")) {
                                method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 18));
                                if(!FMLLaunchHandler.isDeobfuscatedEnvironment()) ((MethodInsnNode)insn).name = "func_180501_a";
                                ((MethodInsnNode)insn).desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z";
                            }
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // this mod rewrites the events in this class, remove old code
            case "com.legacy.nethercraft.world.NetherWorldEvent": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                classNode.methods.removeIf(method -> method.name.equals("onGenerateLand") || method.name.equals("onNetherDecorated"));

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // don't spawn entities on the client (nethercraft is so unfinished ISTG)
            case "com.legacy.nethercraft.entities.projectile.EntitySlimeEggs": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
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
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "onImpact" : "func_70184_a")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("nextInt")) {
                                ((JumpInsnNode)insn.getNext()).setOpcode(IFEQ);
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                                method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/TransformerNethercraftEvents$Hooks", "canSpawnSlime", "(Ljava/util/Random;ILnet/minecraft/entity/Entity;)Z", false));
                                method.instructions.remove(insn);
                                break methods;
                            }
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            // make more of the nethercraft entities fireproof by default
            case "com.legacy.nethercraft.entities.hostile.EntityBloodyZombie":
            case "com.legacy.nethercraft.entities.hostile.EntityDarkZombie":
            case "com.legacy.nethercraft.entities.hostile.EntityLavaSlime": {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                methods:
                for(final MethodNode method : classNode.methods) {
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
                    if(method.name.equals("<init>")) {
                        for(final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn.getOpcode() == RETURN) {
                                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                                method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                                method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/entity/Entity", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "isImmuneToFire" : "func_70045_F", "Z"));
                                break methods;
                            }
                        }
                    }
                }

                //writes the changes
                final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
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
