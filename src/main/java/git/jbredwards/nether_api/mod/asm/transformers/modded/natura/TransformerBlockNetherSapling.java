/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.natura;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.FieldInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack" blocks
 * @author jbred
 *
 */
public final class TransformerBlockNetherSapling implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("getPlantType"), (method, insn) -> {
            /*
             * Old code:
             * return EnumPlantType.Nether;
             *
             * New code:
             * // Support modded "Netherrack" blocks.
             * return git.jbredwards.nether_api.api.util.PlantUtils.NETHER_PLANT_TYPE;
             */
            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("Nether")) {
                method.instructions.insert(insn, new FieldInsnNode(GETSTATIC, "git/jbredwards/nether_api/api/util/PlantUtils", "NETHER_PLANT_TYPE", "Lnet/minecraftforge/common/EnumPlantType;"));
                method.instructions.remove(insn);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }
}
