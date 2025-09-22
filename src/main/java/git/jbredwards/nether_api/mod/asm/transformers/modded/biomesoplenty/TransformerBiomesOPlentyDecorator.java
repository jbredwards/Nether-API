/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.biomesoplenty;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Allow BOP nether features to always generate in the nether
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyDecorator implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        /*
         * Old code:
         * if (!BOPBiomes.excludedDecoratedWorldTypes.contains(event.getWorld().getWorldType()))
         * {
         *     ...
         * }
         *
         * New code:
         * //allow BOP nether features to always generate in the nether
         * if (!Hooks.orNonNether(BOPBiomes.excludedDecoratedWorldTypes.contains(event.getWorld().getWorldType()), event))
         * {
         *     ...
         * }
         */
        return transformMethod(basicClass, method -> true, (method, insn) -> {
            if(insn.getOpcode() == INVOKEINTERFACE && ((MethodInsnNode)insn).name.equals("contains")) {
                method.instructions.insert(insn, genHookMethod("orNonNether", "(ZLjava/lang/Object;)Z"));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean orNonNether(boolean contains, @Nonnull Object event) {
            if(!contains) return false;

            final World world = event instanceof DecorateBiomeEvent ? ((DecorateBiomeEvent)event).getWorld() : ((OreGenEvent)event).getWorld();
            return NetherAPIConfig.BOP.dependentBOPHellBiomes || world.provider.getDimension() != DimensionType.NETHER.getId();
        }
    }
}
