/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.quark;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Fix Quark nether fossil cascading world gen
 * @author jbred
 *
 */
public final class TransformerQuarkCascadingFix implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("generateFossil"), (method, insn) -> {
            /*
             * Old code:
             * Template template = templatemanager.getTemplate(minecraftserver, FOSSILS[i]);
             * 
             * New code:
             * // Fix Quark nether fossil cascading world gen.
             * Template template = templatemanager.getTemplate(minecraftserver, FOSSILS[i]);
             * pos = Hooks.fixPosition(random, pos, rotation, template);
             */
            if(insn.getPrevious() instanceof VarInsnNode && ((VarInsnNode)insn.getPrevious()).var == 9) {
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 6));
                method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 9));
                method.instructions.insertBefore(insn, genHookMethod("fixPosition", "(Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Rotation;Lnet/minecraft/world/gen/structure/template/Template;)Lnet/minecraft/util/math/BlockPos;"));
                method.instructions.insertBefore(insn, new VarInsnNode(ASTORE, 3));
            }
            // Remove structure bounding box, to prevent clipped fossils.
            else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBoundingBox" : "func_186223_a")) {
                method.instructions.remove(insn.getPrevious());
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
        public static BlockPos fixPosition(@Nonnull final Random rand, @Nonnull final BlockPos pos, @Nonnull final Rotation rot, @Nonnull final Template template) {
            @Nonnull final BlockPos size = template.transformedSize(rot);
            return new BlockPos((pos.getX() - 8 & ~15) + 8 + rand.nextInt(16 - size.getX()), pos.getY(), (pos.getZ() - 8 & ~15) + 8 + rand.nextInt(16 - size.getZ()));
        }
    }
}
