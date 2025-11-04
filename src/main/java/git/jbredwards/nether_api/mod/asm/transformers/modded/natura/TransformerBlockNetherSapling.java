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
