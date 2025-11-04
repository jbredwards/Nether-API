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

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.EnumPlantType;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Add support for new EnumPlantTypes to Vanilla soil blocks
 * @author jbred
 * 
 */
public final class TransformerBlock implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, true, method -> method.name.equals("canSustainPlant"), (method, insn) -> {
            /*
             * Old code:
             * {
             *     IBlockState plant = plantable.getPlant(world, pos.offset(direction));
             *     EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
             *     ...
             * }
             *
             * New code:
             * // Add support for new EnumPlantTypes to Vanilla soil blocks.
             * {
             *     IBlockState plant = plantable.getPlant(world, pos.offset(direction));
             *     EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
             *     if(Hooks.isModded(plantType) return Hooks.canSustainPlant(this, plantType);
             *     ...
             * }
             */
            if(insn.getOpcode() == ASTORE && ((VarInsnNode)insn).var == 7) {
                @Nonnull final InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 7));
                list.add(genHookMethod("isModded", "(Lnet/minecraftforge/common/EnumPlantType;)Z"));

                @Nonnull final LabelNode label = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, label));
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 7));
                list.add(genHookMethod("canSustainPlant", "(Lnet/minecraft/block/Block;Lnet/minecraftforge/common/EnumPlantType;)Z"));
                list.add(new InsnNode(IRETURN));

                list.add(label);
                list.add(new FrameNode(F_SAME, 0, null, 0, null));
                method.instructions.insert(insn, list);
                return BreakType.METHODS;
            }
            
            return BreakType.CONTINUE;
        });
    }
    
    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canSustainPlant(@Nonnull final Block soil, @Nonnull final EnumPlantType type) {
            return soil == Blocks.END_STONE && type == PlantUtils.END_PLANT_TYPE ||
                   soil == Blocks.NETHERRACK && (type == PlantUtils.NETHER_PLANT_TYPE || type == PlantUtils.NETHERRACK_PLANT_TYPE) ||
                   soil == Blocks.SOUL_SAND && type == PlantUtils.NETHER_PLANT_TYPE;
        }

        public static boolean isModded(@Nonnull final EnumPlantType type) {
            return type == PlantUtils.END_PLANT_TYPE || type == PlantUtils.NETHER_PLANT_TYPE || type == PlantUtils.NETHERRACK_PLANT_TYPE;
        }
    }
}
