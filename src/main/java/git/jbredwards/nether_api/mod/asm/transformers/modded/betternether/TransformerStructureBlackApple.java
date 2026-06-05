package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.IntInsnNode;

import javax.annotation.Nonnull;

/**
 * Don't let BetterNether forks place black apple plants in the air
 * @author jbred
 *
 */
public final class TransformerStructureBlackApple implements ITransformer
{
    int index = 0;

    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        index = 0;
        return transformMethod(basicClass, method -> method.name.equals("generate"), (method, insn) -> {
            if(insn instanceof IntInsnNode && ((IntInsnNode)insn).operand == 6 && ++index > 1) ((IntInsnNode)insn).operand = 1;
            return BreakType.CONTINUE;
        });
    }
}
