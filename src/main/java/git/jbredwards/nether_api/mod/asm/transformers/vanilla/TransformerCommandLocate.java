/*
 * Copyright (c) 2023-2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Add registered structures to the /locate tab completion list
 * @author jbred
 *
 */
public final class TransformerCommandLocate implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "getTabCompletions" : "func_184883_a"), (method, insn) -> {
            /*
             * getTabCompletions: (changes are around line 66)
             * Old code:
             * return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"}) : Collections.emptyList();
             *
             * New code:
             * //add registered modded structures
             * return args.length == 1 ? getListOfStringsMatchingLastWord(args, Hooks.getStructures(new String[] {"Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"})) : Collections.emptyList();
             */
            if(insn.getOpcode() == INVOKESTATIC && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getListOfStringsMatchingLastWord" : "func_71530_a")) {
                method.instructions.insertBefore(insn, genHookMethod("getStructures", "([Ljava/lang/String;)[Ljava/lang/String;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static String[] getStructures(@Nonnull String[] vanillaStructures) {
            @Nonnull final Set<String> structures = new HashSet<>(Arrays.asList(vanillaStructures));
            INetherAPIRegistry.REGISTRIES.forEach(registry -> registry.getStructures().stream().map(INetherAPIStructureEntry::getCommandName).forEach(structures::add));
            return structures.toArray(new String[0]);
        }
    }
}
