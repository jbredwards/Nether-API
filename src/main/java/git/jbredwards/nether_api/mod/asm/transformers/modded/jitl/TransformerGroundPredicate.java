/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.jitl;

import git.jbredwards.nether_api.api.util.PlantUtils;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.journey.api.block.GroundPredicate;
import org.objectweb.asm.tree.FieldInsnNode;

import javax.annotation.Nonnull;

/**
 * Support modded "Netherrack"-like and "Soul Sand"-like soil blocks
 * @author jbred
 *
 */
public final class TransformerGroundPredicate implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals("<clinit>"), (method, insn) -> {
            /*
             * Old code:
             * GroundPredicate.NETHER = GroundPredicate.SOLID_SIDE.and(GroundPredicate.blockPredicate(block -> { ... }));
             *
             * New code:
             * // Support modded "Netherrack"-like and "Soul Sand"-like soil blocks.
             * GroundPredicate.NETHER = Hooks.wrapNetherCondition(GroundPredicate.SOLID_SIDE.and(GroundPredicate.blockPredicate(block -> { ... })));
             */
            if(insn.getOpcode() == PUTSTATIC && ((FieldInsnNode)insn).name.equals("NETHER")) {
                method.instructions.insertBefore(insn, genHookMethod("wrapNetherCondition", "(Lnet/journey/api/block/GroundPredicate;)Lnet/journey/api/block/GroundPredicate;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static GroundPredicate wrapNetherCondition(@Nonnull final GroundPredicate nether) {
            return nether.or((world, pos, state, direction) -> state.getBlock().canSustainPlant(state, world, pos, direction, PlantUtils.createPlantable(PlantUtils.NETHER_PLANT_TYPE)));
        }
    }
}
