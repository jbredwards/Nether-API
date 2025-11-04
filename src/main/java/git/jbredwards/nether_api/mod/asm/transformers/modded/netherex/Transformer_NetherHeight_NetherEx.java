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

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class Transformer_NetherHeight_NetherEx implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        // BiomeTraitGenerationHandler
        if("logictechcorp.netherex.handler.BiomeTraitGenerationHandler".equals(transformedName)) {
            return transformMethod(basicClass, true, method -> method.name.equals("generateBiomeTraits"), (method, insn) -> {
                /*
                 * generateTerrain:
                 * Old code:
                 * trait.generate(world, pos.add(random.nextInt(16) + 8, RandomHelper.getNumberInRange(trait.getMinimumGenerationHeight(world, pos, random), trait.getMaximumGenerationHeight(world, pos, random), random), random.nextInt(16) + 8), random);
                 *
                 * New code:
                 * // Double gen height to account for world height space
                 * trait.generate(world, pos.add(random.nextInt(16) + 8, RandomHelper.getNumberInRange(trait.getMinimumGenerationHeight(world, pos, random), trait.getMaximumGenerationHeight(world, pos, random), random) + (world.getActualHeight() >> 8 << 7), random.nextInt(16) + 8), random);
                 */
                if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("getMaximumGenerationHeight")) {
                    @Nonnull final InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(genHeightOffset(true));
                    method.instructions.insert(insn, list);
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        }

        // BiomeDataNetherEx
        else if("logictechcorp.netherex.world.biome.data.BiomeDataNetherEx".equals(transformedName)) {
            return transformMethod(basicClass, method -> method.name.equals("generateTerrain"), (method, insn) -> {
                /*
                 * generateTerrain:
                 * Old code:
                 * for (int posY = 127; posY >= 0; posY--)
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Use actual nether height instead of a hardcoded value
                 * for (int posY = world.getActualHeight() - 1; posY >= 0; posY--)
                 * {
                 *     ...
                 * }
                 */
                if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 127) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                    method.instructions.insertBefore(insn, genHeightMethod());
                    method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                    method.instructions.insertBefore(insn, new InsnNode(ISUB));
                    method.instructions.remove(insn);
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        }

        return basicClass;
    }
}
