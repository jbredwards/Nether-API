/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Disable NetherEx's nether override
 * @author jbred
 *
 */
public final class TransformerNetherExOverride implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // NetherEx
        if("logictechcorp.netherex.NetherEx".equals(transformedName)) {
            return transform(basicClass, classNode -> {
                classNode.methods.removeIf(method -> method.name.equals("onFMLServerStarting")); // remove nether world provider override
                /*
                 * onFMLInitialization & onFMLPostInitialization:
                 * Old code:
                 * if(NetherExConfig.dimension.nether.overrideNether)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Always properly register NetherEx's biomes
                 * if(true)
                 * {
                 *     ...
                 * }
                 */
                transformMethod(classNode, method -> method.name.equals("onFMLInitialization") || method.name.equals("onFMLPostInitialization"), (method, insn) -> {
                    if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("overrideNether")) {
                        method.instructions.insert(insn, new InsnNode(ICONST_1));
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn);
                        return BreakType.INSTRUCTIONS;
                    }

                    return BreakType.CONTINUE;
                });
            });
        }

        // BiomeTraitGenerationHandler & BiomeDataManagerNetherEx
        else if("logictechcorp.netherex.handler.BiomeTraitGenerationHandler".equals(transformedName)
             || "logictechcorp.netherex.world.biome.data.BiomeDataManagerNetherEx".equals(transformedName)) {
            return transformMethod(basicClass, method -> method.name.equals("generateBiomeTraits") || method.name.equals("onWorldLoad"), (method, insn) -> {
                /*
                 * generateBiomeTraits | onWorldLoad:
                 * Old code:
                 * if(NetherExConfig.dimension.nether.overrideNether)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Always properly register NetherEx's biomes
                 * if(true)
                 * {
                 *     ...
                 * }
                 */
                if(insn.getOpcode() == GETFIELD && ((FieldInsnNode)insn).name.equals("overrideNether")) {
                    method.instructions.insert(insn, new InsnNode(ICONST_1));
                    method.instructions.remove(insn.getPrevious());
                    method.instructions.remove(insn.getPrevious());
                    method.instructions.remove(insn);
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        }

        return basicClass;
    }
}
