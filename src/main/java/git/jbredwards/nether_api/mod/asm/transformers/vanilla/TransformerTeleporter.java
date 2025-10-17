/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Allow for custom obsidian platform generation
 * @author jbred
 *
 */
public final class TransformerTeleporter implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "placeInPortal" : "func_180266_a"), (method, insn) -> {
            /*
             * Old code:
             * for (int j1 = -2; j1 <= 2; ++j1)
             * {
             *     ...
             * }
             *
             * New code:
             * // Allow for custom obsidian platform generation.
             * for (int j1 = -2; j1 <= Hooks.generateObsidianPlatform(this.world, entityIn); ++j1)
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == ICONST_2) {
                method.instructions.insert(insn, genHookMethod("generateObsidianPlatform", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)I"));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
                method.instructions.insert(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/Teleporter", DEOBFUSCATED ? "world" : "field_85192_a", "Lnet/minecraft/world/WorldServer;"));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insert(insn, new InsnNode(POP));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static int generateObsidianPlatform(@Nonnull final World world, @Nonnull final Entity entity) {
            WorldProviderTheEnd.OBSIDIAN_PLATFORM.generate(world, new Random(world.getSeed()), new BlockPos(entity.posX, entity.posY, entity.posZ));
            return -1000;
        }
    }
}
