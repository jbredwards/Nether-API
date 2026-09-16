package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.network.MessageTeleportFX;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;

/**
 * Fix dragon egg teleport particle desync and teleporting into the air
 * @author jbred
 *
 */
public final class TransformerBlockDragonEgg implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "teleport" : "func_180684_e"), (method, insn) -> {
            /*
             * teleport: (changes are around line 113)
             * Old code:
             * if (worldIn.isAirBlock(blockpos))
             * {
             *     ...
             * }
             *
             * New code:
             * // Fix dragon egg teleporting into the air
             * if (Hooks.isPositionValid(worldIn, blockpos))
             * {
             *     ...
             * }
             */
            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "isAirBlock" : "func_175623_d")) {
                method.instructions.insert(insn, genHookMethod("isPositionValid", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z"));
                method.instructions.remove(insn);
            }
            /*
             * teleport: (changes are around line 117)
             * Old code:
             * for (int j = 0; j < 128; ++j)
             * {
             *     ...
             * }
             *
             * New code:
             * // Disable old broken particle logic
             * for (int j = 0; j < Hooks.oldParticles(128); ++j)
             * {
             *     ...
             * }
             */
            else if(insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 128) {
                method.instructions.insert(insn, genHookMethod("oldParticles", "(I)I"));
            }
            /*
             * teleport: (changes are around line 132)
             * Old code:
             * worldIn.setBlockToAir(pos);
             *
             * New code:
             * // Send teleport positions to client, so it can spawn the correct particles
             * worldIn.setBlockToAir(pos);
             * Hooks.sendParticles(worldIn, pos, blockpos);
             */
            else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setBlockToAir" : "func_175698_g")) {
                method.instructions.insert(insn, genHookMethod("sendParticles", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 5));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 2));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean isPositionValid(@Nonnull final World world, @Nonnull final BlockPos pos) {
            return world.isValid(pos) && world.isAirBlock(pos) && world.isSideSolid(pos.down(), EnumFacing.UP);
        }

        public static int oldParticles(final int amount) {
            return Transformer_MC_10369.Hooks.TELEPORT_FIX ? 0 : amount;
        }

        public static void sendParticles(@Nonnull final World world, @Nonnull final BlockPos oldPos, @Nonnull final BlockPos newPos) {
            if(oldParticles(1) == 0) NetherAPI.WRAPPER.sendToAllTracking(new MessageTeleportFX(newPos, oldPos),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), oldPos.getX(), oldPos.getY(), oldPos.getZ(), 0));
        }
    }
}
