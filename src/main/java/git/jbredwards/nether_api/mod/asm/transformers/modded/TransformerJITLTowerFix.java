/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Fix Journey Into The Light's WorldGenNetherTower using bad registry names for spawners (causing lots of log spam and broken spawners)
 * @author jbred
 *
 */
public final class TransformerJITLTowerFix implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.journey.dimension.nether.gen.WorldGenNetherTower".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                /*
                 * generate:
                 * Old code:
                 * mobNames.add("LavaSlime");
                 * mobNames.add("PigZombie");
                 * mobNames.add("lavasnake");
                 * mobNames.add("reaper");
                 *
                 * New code:
                 * // Add proper namespace
                 * mobNames.add("minecraft:magma_cube");
                 * mobNames.add("minecraft:zombie_pigman");
                 * mobNames.add("journey:lavasnake");
                 * mobNames.add("journey:reaper");
                 */
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "generate" : "func_180709_b")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        if(insn.getOpcode() == LDC) {
                            if(((LdcInsnNode)insn).cst.equals("LavaSlime")) ((LdcInsnNode)insn).cst = "minecraft:magma_cube";
                            else if(((LdcInsnNode)insn).cst.equals("PigZombie")) ((LdcInsnNode)insn).cst = "minecraft:zombie_pigman";
                            else if(((LdcInsnNode)insn).cst.equals("lavasnake")) ((LdcInsnNode)insn).cst = "journey:lavasnake";
                            else if(((LdcInsnNode)insn).cst.equals("reaper")) {
                                ((LdcInsnNode)insn).cst = "journey:reaper";
                                break methods;
                            }
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
