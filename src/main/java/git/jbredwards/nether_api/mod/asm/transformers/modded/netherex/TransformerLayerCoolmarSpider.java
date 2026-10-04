/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;

/**
 * Fix Coolmar Spider rendering
 * @author jbred
 *
 */
public final class TransformerLayerCoolmarSpider implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "doRenderLayer" : "func_177141_a"), (method, insn) -> {
            /*
             * New code:
             * // Fix Coolmar Spider rendering.
             * {
             *     ...
             *     GlStateManager.depthMask(true);
             * }
             */
            if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "enableAlpha" : "func_179141_d")) {
                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", DEOBFUSCATED ? "depthMask" : "func_179132_a", "(Z)V", false));
                method.instructions.insert(insn, new InsnNode(ICONST_1));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
