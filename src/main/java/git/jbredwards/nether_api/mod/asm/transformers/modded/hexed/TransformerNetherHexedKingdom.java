/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.hexed;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.IntSupplier;

/**
 * Allow structures to work with increased nether height, and fix some cascading world gen issues
 * @author jbred
 *
 */
public final class TransformerNetherHexedKingdom implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        if(transformedName.startsWith("com.deimoshexxus.netherhexedkingdommod.world.generators.")) {
            @Nonnull final String structure = transformedName.substring("com.deimoshexxus.netherhexedkingdommod.world.generators.".length()).replaceFirst("WorldGen", "").replaceFirst("Nether", "");
            return transformMethod(basicClass, method -> method.name.equals("generate"), (method, insn) -> {
                // Offset generation position depending on structure size.
                if(insn.getOpcode() == INVOKESPECIAL && ((MethodInsnNode)insn).owner.equals("net/minecraft/util/math/BlockPos") && ((MethodInsnNode)insn).desc.equals("(III)V")) {
                    method.instructions.insert(insn, genHookMethod("fixPosition", "(Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;L" + genHookClass() + ";I)Lnet/minecraft/util/math/BlockPos;"));
                    switch(structure) {
                        case "BullionTemple": method.instructions.insert(insn, new InsnNode(ICONST_4)); break;
                        case "LostOutpost": method.instructions.insert(insn, new InsnNode(ICONST_5)); break;
                        case "TowerOfRedSun": method.instructions.insert(insn, new IntInsnNode(BIPUSH, 7)); break;
                        case "DamnedPrison":
                        case "GreedMines": method.instructions.insert(insn, new InsnNode(ICONST_0)); break;
                        case "IronClad":
                        case "WretchedLookout": method.instructions.insert(insn, new InsnNode(ICONST_3)); break;
                        default: method.instructions.insert(insn, new IntInsnNode(BIPUSH, 8));
                    }

                    method.instructions.insert(insn, genHookField(structure, 'L' + genHookClass() + ';'));
                    method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
                }

                // (WorldGenNetherIronClad) don't check blocks wildly out of bounds???
                else if(transformedName.endsWith("d")) {
                    if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "east" : "func_177965_g")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn, new IntInsnNode(BIPUSH, 16));
                    }
                    else if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "west" : "func_177985_f")) {
                        method.instructions.remove(insn.getPrevious());
                        method.instructions.insertBefore(insn, new InsnNode(ICONST_1));
                    }
                }

                // Use better block flags.
                else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "addBlocksToWorld" : "func_186253_b")) {
                    method.instructions.insert(insn, new MethodInsnNode(INVOKEVIRTUAL, ((MethodInsnNode)insn).owner, DEOBFUSCATED ? "addBlocksToWorld" : "func_189962_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/structure/template/PlacementSettings;I)V", false));
                    method.instructions.insert(insn, genBlockFlags());
                    method.instructions.remove(insn);
                }

                return BreakType.CONTINUE;
            });
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public enum Hooks
    {
        BullionTemple(() -> NetherAPIConfig.NetherHexedKingdom.bullionTempleMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.bullionTempleMaxHeight),
        DamnedPrison(() -> NetherAPIConfig.NetherHexedKingdom.damnedPrisonMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.damnedPrisonMaxHeight),
        GreedMines(() -> NetherAPIConfig.NetherHexedKingdom.greedMinesMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.greedMinesMaxHeight),
        IronClad(() -> NetherAPIConfig.NetherHexedKingdom.ironCladMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.ironCladMaxHeight),
        LostOutpost(() -> NetherAPIConfig.NetherHexedKingdom.lostOutpostMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.lostOutpostMaxHeight),
        MagmaCubeNest(() -> NetherAPIConfig.NetherHexedKingdom.magmaCubeNestMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.magmaCubeNestMaxHeight),
        TowerOfRedSun(() -> NetherAPIConfig.NetherHexedKingdom.towerOfRedSunMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.towerOfRedSunMaxHeight),
        WrathTower(() -> NetherAPIConfig.NetherHexedKingdom.wrathTowerMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.wrathTowerMaxHeight),
        WretchedLookout(() -> NetherAPIConfig.NetherHexedKingdom.wretchedLookoutMinHeight, () -> NetherAPIConfig.NetherHexedKingdom.wretchedLookoutMaxHeight);

        @Nonnull
        private final IntSupplier minHeightForGen, maxHeightForGen;
        Hooks(@Nonnull final IntSupplier minHeightIn, @Nonnull final IntSupplier maxHeightIn) {
            minHeightForGen = minHeightIn;
            maxHeightForGen = maxHeightIn;
        }

        // helper
        public int getMinHeightForGen() { return minHeightForGen.getAsInt(); }

        // helper
        public int getMaxHeightForGen() { return maxHeightForGen.getAsInt(); }

        @Nonnull
        public static BlockPos fixPosition(@Nonnull final BlockPos pos, @Nonnull final Random rand, @Nonnull final Hooks settings, final int offset) {
            return new BlockPos(pos.getX() - 8 + offset, MathHelper.getInt(rand, settings.getMinHeightForGen(), settings.getMaxHeightForGen()), pos.getZ() - 8 + offset);
        }
    }
}
