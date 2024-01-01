/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded;

import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * Allow BOP nether features to always generate in the nether
 * @author jbred
 *
 */
public final class TransformerBiomesOPlentyDecorator implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.equals("biomesoplenty.common.handler.decoration.DecorateBiomeEventHandler")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            for(final MethodNode method : classNode.methods) {
                for(final AbstractInsnNode insn : method.instructions.toArray()) {
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
                    if(insn.getOpcode() == INVOKEINTERFACE && ((MethodInsnNode)insn).name.equals("contains")) {
                        method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/modded/TransformerBiomesOPlentyDecorator$Hooks", "orNonNether", "(ZLjava/lang/Object;)Z", false));
                        method.instructions.insert(insn, new VarInsnNode(ALOAD, 1));
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
        public static boolean orNonNether(boolean contains, @Nonnull Object event) {
            if(!contains) return false;

            final World world = event instanceof DecorateBiomeEvent ? ((DecorateBiomeEvent)event).getWorld() : ((OreGenEvent)event).getWorld();
            return NetherAPIConfig.BOP.dependentBOPHellBiomes || world.provider.getDimension() != DimensionType.NETHER.getId();
        }
    }
}
