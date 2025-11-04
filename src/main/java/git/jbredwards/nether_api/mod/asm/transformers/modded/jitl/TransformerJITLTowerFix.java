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

package git.jbredwards.nether_api.mod.asm.transformers.modded.jitl;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.LdcInsnNode;

import javax.annotation.Nonnull;

/**
 * Fix Journey Into The Light's WorldGenNetherTower using bad registry names for spawners (causing lots of log spam and broken spawners)
 * @author jbred
 *
 */
public final class TransformerJITLTowerFix implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
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
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "generate" : "func_180709_b"), (method, insn) -> {
            if(insn.getOpcode() == LDC) {
                if(((LdcInsnNode)insn).cst.equals("LavaSlime")) ((LdcInsnNode)insn).cst = "minecraft:magma_cube";
                else if(((LdcInsnNode)insn).cst.equals("PigZombie")) ((LdcInsnNode)insn).cst = "minecraft:zombie_pigman";
                else if(((LdcInsnNode)insn).cst.equals("lavasnake")) ((LdcInsnNode)insn).cst = "journey:lavasnake";
                else if(((LdcInsnNode)insn).cst.equals("reaper")) {
                    ((LdcInsnNode)insn).cst = "journey:reaper";
                    return BreakType.METHODS;
                }
            }

            return BreakType.CONTINUE;
        });
    }
}
