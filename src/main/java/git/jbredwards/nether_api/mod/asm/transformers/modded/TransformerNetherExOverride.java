package git.jbredwards.nether_api.mod.asm.transformers.modded;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;

/**
 * Disable NetherEx's nether override
 * @author jbred
 *
 */
public final class TransformerNetherExOverride implements IClassTransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("logictechcorp.netherex.NetherEx".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            //remove method
            classNode.methods.removeIf(method -> method.name.equals("onFMLServerStarting"));

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
