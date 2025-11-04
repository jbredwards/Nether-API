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

package git.jbredwards.nether_api.mod.asm.transformers.modded.justenoughdimensions;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;

/**
 * Account for modded DerivedWorldInfo instances
 * @author jbred
 *
 */
public final class TransformerWorldInfoUtils implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("loadAndSetCustomWorldInfo"), (method, insn) -> {
            /*
             * Old code:
             * if (world.getWorldInfo().getClass() == DerivedWorldInfo.class)
             * {
             *     ...
             * }
             *
             * New code:
             * // Account for modded DerivedWorldInfo instances.
             * if (Hooks.getClass(world.getWorldInfo()) == DerivedWorldInfo.class)
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("getClass")) {
                method.instructions.insert(insn, genHookMethod("getClass", "(Lnet/minecraft/world/storage/WorldInfo;)Ljava/lang/Class;"));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Class<?> getClass(@Nonnull final WorldInfo info) {
            return info instanceof DerivedWorldInfo ? DerivedWorldInfo.class : Object.class;
        }
    }
}
