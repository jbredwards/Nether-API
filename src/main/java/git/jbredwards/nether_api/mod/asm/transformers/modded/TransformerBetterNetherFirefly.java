package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Fix BetterNether firefly spawning code
 * @author jbred
 *
 */
public final class TransformerBetterNetherFirefly implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("paulevs.betternether.entities.EntityFirefly".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            //override method
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getCanSpawnHere" : "func_70601_bi")) {
                    //remove existing body data
                    method.instructions.clear();
                    if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
                    if(method.localVariables != null) method.localVariables.clear();
                    if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
                    if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();

                    //write new body data
                    final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                    generator.visitInsn(ICONST_1);
                    generator.visitInsn(IRETURN);
                    break;
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
