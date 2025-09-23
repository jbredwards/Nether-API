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
        switch(transformedName) {
            // NetherEx
            case "logictechcorp.netherex.NetherEx": {
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
            case "logictechcorp.netherex.handler.BiomeTraitGenerationHandler":
            case "logictechcorp.netherex.world.biome.data.BiomeDataManagerNetherEx": {
                return transformMethod(basicClass, method -> method.name.equals("generateBiomeTraits") || method.name.equals("onWorldLoad") || method.name.equals("onWorldUnload"), (method, insn) -> {
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
                        return method.name.equals("onWorldLoad") ? BreakType.INSTRUCTIONS : BreakType.METHODS;
                    }
                    /*
                     * onWorldLoad | onWorldUnload:
                     * Old code:
                     * if(world.provider.getDimension() == DimensionType.OVERWORLD.getId())
                     * {
                     *     ...
                     * }
                     *
                     * New code:
                     * // Always properly register NetherEx's biomes
                     * if(world.provider.getDimension() == DimensionType.NETHER.getId())
                     * {
                     *     ...
                     * }
                     */
                    else if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("OVERWORLD")) {
                        ((FieldInsnNode)insn).name = "NETHER";
                        if(method.name.equals("onWorldUnload")) return BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }

            // WorldHandler
            case "logictechcorp.netherex.handler.WorldHandler": {
                return transformMethod(basicClass, method -> method.name.equals("onWorldLoad") || method.name.equals("onWorldUnload"), (method, insn) -> {
                    /*
                     * onWorldLoad | onWorldUnload:
                     * Old code:
                     * {
                     *     NetherEx.BIOME_DATA_MANAGER.onWorldLoad(event);
                     *     ...
                     * }
                     * {
                     *     NetherEx.BIOME_DATA_MANAGER.onWorldUnload(event);
                     *     ...
                     * }
                     *
                     * New code:
                     * // Move NetherEx data handling to compat handler, so it can be loaded during registry loading
                     * {
                     *     ...
                     * }
                     * {
                     *     ...
                     * }
                     */
                    if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals(method.name)) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.remove(insn);
                        return method.name.equals("onWorldLoad") ? BreakType.INSTRUCTIONS : BreakType.METHODS;
                    }

                    return BreakType.CONTINUE;
                });
            }
        }

        return basicClass;
    }
}
