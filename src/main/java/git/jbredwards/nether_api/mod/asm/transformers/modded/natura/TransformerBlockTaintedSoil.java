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

import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraftforge.common.EnumPlantType;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Add support for new EnumPlantType to tainted soil
 * @author jbred
 *
 */
public final class TransformerBlockTaintedSoil implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("canSustainPlant"), (method, insn) -> {
            /*
             * Old code:
             * return plantType == EnumPlantType.Nether ? true : super.canSustainPlant(state, world, pos, direction, plantable);
             *
             * New code:
             * // Add support for new EnumPlantType to tainted soil.
             * return plantType == Hooks.getPlantType(plantType, EnumPlantType.Nether) ? true : super.canSustainPlant(state, world, pos, direction, plantable);
             */
            if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals("Nether")) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 6));
                method.instructions.insert(insn, genHookMethod("getPlantType", "(Lnet/minecraftforge/common/EnumPlantType;Lnet/minecraftforge/common/EnumPlantType;)Lnet/minecraftforge/common/EnumPlantType;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static EnumPlantType getPlantType(@Nonnull final EnumPlantType plantType, @Nonnull final EnumPlantType fallback) {
            return PlantUtils.sustainsNether(PlantUtils.NETHER_PLANT_TYPE, plantType) ? plantType : fallback;
        }
    }
}
