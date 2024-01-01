/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Add registered structures to the /locate tab completion list
 * @author jbred
 *
 */
public final class TransformerCommandLocate implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if(transformedName.equals("net.minecraft.command.CommandLocate")) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getTabCompletions" : "func_184883_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * getTabCompletions: (changes are around line 66)
                         * Old code:
                         * return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"}) : Collections.emptyList();
                         *
                         * New code:
                         * //add registered modded structures
                         * return args.length == 1 ? getListOfStringsMatchingLastWord(args, Hooks.getStructures(new String[] {"Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"})) : Collections.emptyList();
                         */
                        if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getListOfStringsMatchingLastWord" : "func_71530_a")) {
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerCommandLocate$Hooks", "getStructures", "([Ljava/lang/String;)[Ljava/lang/String;", false));
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
        @Nonnull
        public static String[] getStructures(@Nonnull String[] vanillaStructures) {
            final List<String> structures = new LinkedList<>(Arrays.asList(vanillaStructures));
            INetherAPIRegistry.REGISTRIES.forEach(registry -> registry.getStructures().stream().map(INetherAPIStructureEntry::getCommandName).forEach(structures::add));
            return structures.toArray(new String[0]);
        }
    }
}
