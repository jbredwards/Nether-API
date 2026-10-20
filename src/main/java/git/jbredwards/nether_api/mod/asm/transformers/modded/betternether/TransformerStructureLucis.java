/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Don't generate lucis mushroom if it'll intersect with another lucis mushroom
 * @author jbred
 *
 */
public final class TransformerStructureLucis implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, true, method -> method.name.equals("generate"), (method, insn) -> {
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("nextInt")) {
                final int index = method.localVariables.stream().mapToInt(local -> local.index).max().orElse(-1) + 1;
                // Inject new local variable.
                @Nonnull final LabelNode start = new LabelNode();
                @Nonnull final LabelNode end = new LabelNode();
                method.instructions.insert(method.instructions.getFirst(), start);
                method.instructions.insertBefore(method.instructions.getLast(), end);
                method.localVariables.add(new LocalVariableNode("nether_api$type", "I", null, start, end, index));
                /*
                 * Old code:
                 * if (random.nextInt(3) == 0)
                 * {
                 *     ...
                 * }
                 * else
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Don't generate if it'll intersect with another lucis mushroom.
                 * int nether_api$type = random.nextInt(3);
                 * nether_api$type = Hooks.intersectsLarge(world, pos, nether_api$type);
                 * if (Hooks.intersects(world, pos, nether_api$type))
                 * {
                 *     return;
                 * }
                 * if (nether_api$type == 0)
                 * {
                 *     ...
                 * }
                 * else
                 * {
                 *     ...
                 * }
                 */
                @Nonnull final LabelNode label = new LabelNode();
                @Nonnull final InsnList list = new InsnList();
                list.add(new VarInsnNode(ISTORE, index));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 2));
                list.add(new VarInsnNode(ILOAD, index));
                list.add(genHookMethod("intersectsLarge", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)I"));
                list.add(new VarInsnNode(ISTORE, index));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 2));
                list.add(new VarInsnNode(ILOAD, index));
                list.add(genHookMethod("intersectsAny", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)Z"));
                list.add(new JumpInsnNode(IFEQ, label));
                list.add(new InsnNode(RETURN));
                list.add(label);
                list.add(new FrameNode(F_SAME, 0, null, 0, null));
                list.add(new VarInsnNode(ILOAD, index));
                method.instructions.insert(insn, list);
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        // Helper.
        public static boolean cannotBeIntersectedAt(@Nonnull final World world, @Nonnull final BlockPos pos) {
            @Nonnull final IBlockState state = world.getBlockState(pos);
            return !state.getBlock().isAir(state, world, pos) && !state.isFullBlock();
        }
        
        public static boolean intersectsAny(@Nonnull final World world, @Nonnull final BlockPos pos, final int type) {
            if(type == 0) return false; // Is large type, and passed its check.

            @Nonnull final BlockPos origin;
            final boolean offset;
            if(world.getBlockState(pos.north()).isFullBlock()) {
                origin = pos.north();
                offset = true;
            }
            else if(world.getBlockState(pos.east()).isFullBlock()) {
                origin = pos.east();
                offset = true;
            }
            else {
                origin = pos;
                offset = false;
            }

            if(!offset && cannotBeIntersectedAt(world, pos)) return true;
            else if(cannotBeIntersectedAt(world, origin.west())) return true;
            else if(cannotBeIntersectedAt(world, origin.south())) return true;
            else return cannotBeIntersectedAt(world, origin.south().west());
        }

        public static int intersectsLarge(@Nonnull final World world, @Nonnull final BlockPos pos, final int type) {
            if(type != 0) return type; // Not a large mushroom, pass check.

            else if(cannotBeIntersectedAt(world, pos)) return 1;
            else if(cannotBeIntersectedAt(world, pos.north())) return 1;
            else if(cannotBeIntersectedAt(world, pos.south())) return 1;
            else if(cannotBeIntersectedAt(world, pos.east())) return 1;
            else if(cannotBeIntersectedAt(world, pos.west())) return 1;

            else if(cannotBeIntersectedAt(world, pos.north().east())) return 1;
            else if(cannotBeIntersectedAt(world, pos.north().west())) return 1;
            else if(cannotBeIntersectedAt(world, pos.south().east())) return 1;
            else if(cannotBeIntersectedAt(world, pos.south().west())) return 1;

            return 0;
        }
    }
}
