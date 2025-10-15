/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.netherex;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBlockChorusFlower;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Soul Sand"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerBlockThornstalk implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            transformPlantable(classNode, "SOUL_SAND_PLANT_TYPE");
            transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"), (method, insn) -> {
                /*
                 * Old code:
                 * return (blockDown != this || blockDown2 != this || blockDown3 != this) && block == Blocks.AIR && (blockDown == this || blockDown == Blocks.SOUL_SAND);
                 *
                 * New code:
                 *  // Support modded "Soul Sand"-like soil blocks.
                 * return (blockDown != this || blockDown2 != this || blockDown3 != this) && block == Blocks.AIR && (blockDown == this || blockDown == Hooks.getSoilBlock(this, world, pos.down(), Blocks.SOUL_SAND));
                 */
                if(insn.getOpcode() == GETSTATIC && ((FieldInsnNode)insn).name.equals(DEOBFUSCATED ? "SOUL_SAND" : "field_150425_aM")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                    method.instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                    method.instructions.insert(insn, genHookMethod("getSoilBlock", "(Lnet/minecraftforge/common/IPlantable;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;"));
                    return BreakType.METHODS;
                }

                return BreakType.CONTINUE;
            });
        });
    }

    @Nonnull
    @Override
    public String genHookClass() { return new TransformerBlockChorusFlower().genHookClass(); }
}
