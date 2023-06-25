package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.biome.BiomeHell;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * All BiomeHell instances use netherrack as their top and filler blocks
 * @author jbred
 *
 */
public final class TransformerBiomeHell implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("net.minecraft.world.biome.BiomeHell")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals("<init>")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * Constructor: (changes are around line 21)
                         * Old code:
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * //set default top and filler blocks
                         * {
                         *     ...
                         *     Hooks.setDefaultTopAndFillerBlocks(this);
                         * }
                         */
                        if(insn.getOpcode() == RETURN) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerBiomeHell$Hooks", "setDefaultTopAndFillerBlocks", "(Lnet/minecraft/world/biome/BiomeHell;)V", false));
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void setDefaultTopAndFillerBlocks(@Nonnull BiomeHell biome) {
            biome.topBlock = Blocks.NETHERRACK.getDefaultState();
            biome.fillerBlock = Blocks.NETHERRACK.getDefaultState();
        }
    }
}
