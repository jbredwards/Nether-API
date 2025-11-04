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

import fi.dy.masa.justenoughdimensions.world.WorldProviderEndJED;
import fi.dy.masa.justenoughdimensions.world.WorldProviderHellJED;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.compat.justenoughdimensions.JEDWorldProviderNether;
import git.jbredwards.nether_api.mod.common.compat.justenoughdimensions.JEDWorldProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;

/**
 * Ensure compatible Nether API world providers are used instead of old ones
 * @author jbred
 *
 */
public final class TransformerDimensionTypeEntry implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> Modifier.isStatic(method.access) && method.name.equals("getProviderClass"), (method, insn) -> {
            /*
             * Old code:
             * return providerClass;
             *
             * New code:
             * // Ensure compatible Nether API world providers are used instead of old ones.
             * return Hooks.getCompatibleProvider(providerClass);
             */
            if(insn.getOpcode() == ARETURN) method.instructions.insertBefore(insn, genHookMethod("getCompatibleProvider", "(Ljava/lang/Class;)Ljava/lang/Class;"));
            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nullable
        public static Class<? extends WorldProvider> getCompatibleProvider(@Nullable final Class<? extends WorldProvider> provider) {
            if(provider == WorldProviderEnd.class) return WorldProviderTheEnd.class;
            else if(provider == WorldProviderEndJED.class) return JEDWorldProviderTheEnd.class;

            else if(provider == WorldProviderHell.class) return WorldProviderNether.class;
            else if(provider == WorldProviderHellJED.class) return JEDWorldProviderNether.class;

            return provider;
        }
    }
}
