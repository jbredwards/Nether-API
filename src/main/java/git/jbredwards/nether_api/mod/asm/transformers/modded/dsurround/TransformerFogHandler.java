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

package git.jbredwards.nether_api.mod.asm.transformers.modded.dsurround;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.objectweb.asm.tree.MethodInsnNode;
import org.orecruncher.dsurround.client.handlers.fog.FogResult;

import javax.annotation.Nonnull;

/**
 * Account for world.provider.doesXZShowFog().
 * @author jbred
 *
 */
public final class TransformerFogHandler implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("fogRenderEvent"), (method, insn) -> {
            /*
             * Old code:
             * FogResult result = this.fogRange.calculate(event);
             *
             * New code:
             * // Account for world.provider.doesXZShowFog().
             * FogResult result = Hooks.applyXZShowFog(this.fogRange.calculate(event));
             */
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("calculate")) {
                method.instructions.insert(insn, genHookMethod("applyXZShowFog", "(Lorg/orecruncher/dsurround/client/handlers/fog/FogResult;)Lorg/orecruncher/dsurround/client/handlers/fog/FogResult;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static FogResult applyXZShowFog(@Nonnull final FogResult result) {
            @Nonnull final Minecraft mc = Minecraft.getMinecraft();
            @Nonnull final Entity entity = mc.getRenderViewEntity();

            return mc.world.provider.doesXZShowFog((int)entity.posX, (int)entity.posZ) || mc.ingameGUI.getBossOverlay().shouldCreateFog() ? new FogResult(result.getStart() * 0.05f, Math.min(192, result.getEnd()) * 0.5f) : result;
        }
    }
}
