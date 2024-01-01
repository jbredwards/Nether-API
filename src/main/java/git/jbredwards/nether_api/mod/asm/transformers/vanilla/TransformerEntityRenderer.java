/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Fix MC-31681 (Fog and clouds darken when indoors or under trees)
 * @author jbred
 *
 */
public final class TransformerEntityRenderer implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.minecraft.client.renderer.EntityRenderer".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            methods:
            for(final MethodNode method : classNode.methods) {
                // updateRenderer
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "updateRenderer" : "func_78464_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * updateRenderer: (changes are around line 355)
                         * Old code:
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Fix MC-31681 (Fog and clouds darken when indoors or under trees)
                         * {
                         *     ...
                         *     this.fogColor1 = 1F;
                         * }
                         */
                        if(insn.getOpcode() == RETURN) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new InsnNode(FCONST_1));
                            method.instructions.insertBefore(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/client/renderer/EntityRenderer", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "fogColor1" : "field_78539_ae", "F"));
                            break;
                        }
                    }
                }
                // setupFogColor
                /*else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setupFogColor" : "func_191514_d")) {
                    int passes = 0;
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * setupFogColor: (changes are around lines 2058 & 2062)
                         * Old code:
                         * GlStateManager.glFog(2918, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                         * ...
                         * GlStateManager.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, Hooks.getFogAlpha(this.mc)));
                         *
                         * New code:
                         * // Make end fog render much more translucently
                         * GlStateManager.glFog(2918, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                         * ...
                         * GlStateManager.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, Hooks.getFogAlpha(this.mc)));
                         */
                        /*if(insn.getOpcode() == FCONST_1) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/EntityRenderer", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "mc" : "field_78531_r", "Lnet/minecraft/client/Minecraft;"));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerEntityRenderer$Hooks", "getFogAlpha", "(Lnet/minecraft/client/Minecraft;)F", false));
                            method.instructions.remove(insn);
                            if(++passes == 2) break methods;
                        }
                    }
                }*/
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @SideOnly(Side.CLIENT)
        public static float getFogAlpha(@Nonnull Minecraft mc) { return mc.world.provider.getDimension() == DimensionType.THE_END.getId() ? 0.01f : 1; }
    }
}
