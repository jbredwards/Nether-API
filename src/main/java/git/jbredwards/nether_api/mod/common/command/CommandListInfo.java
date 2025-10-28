/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.command;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.TextTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public class CommandListInfo extends CommandBase
{
    @Nonnull
    @Override
    public String getName() { return "list"; }

    @Nonnull
    @Override
    public String getUsage(@Nonnull final ICommandSender sender) {
        return "commands." + NetherAPI.MODID + '.' + getName() + ".usage";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args, @Nullable final BlockPos targetPos) {
        if(args.length == 1) return getListOfStringsMatchingLastWord(args, "biomes", "structures");
        else if(args.length != 2) return Collections.emptyList();

        else return getListOfStringsMatchingLastWord(args, INetherAPIRegistry.REGISTRIES.stream().map(INetherAPIRegistry::getRegistryName).collect(Collectors.toSet()));
    }

    @Override
    public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args) throws CommandException {
        @Nullable final String type = args.length < 1 ? null : args[0];
        if(type != null && !type.equals("biomes") && !type.equals("structures")) throw new WrongUsageException(getUsage(sender));

        @Nullable final ResourceLocation registry = args.length < 2 ? null : new ResourceLocation(args[1]);
        @Nonnull final INetherAPIRegistry[] registries = INetherAPIRegistry.REGISTRIES.stream()
                .filter(registry == null ? reg -> true : reg -> reg.getRegistryName().equals(registry))
                .sorted(Comparator.comparing(INetherAPIRegistry::getRegistryName))
                .toArray(INetherAPIRegistry[]::new);

        if(registries.length == 0) throw new CommandException("commands." + NetherAPI.MODID + '.' + getName() + ".fail", String.valueOf(registry));
        else printInfo(sender, type, registries);
    }

    protected static void printInfo(@Nonnull final ICommandSender sender, @Nullable final String type, @Nonnull final INetherAPIRegistry[] registries) {
        if(type == null || type.equals("biomes")) {
            @Nonnull final TextTable table = new TextTable(Arrays.asList(TextTable.column("Registry"), TextTable.column("Biome"), TextTable.column("Weight")));
            for(@Nonnull final INetherAPIRegistry registry : registries) registry.getBiomeEntries().stream()
                    .filter(entry -> entry.biome.getRegistryName() != null)
                    .sorted(Comparator.comparing(entry -> entry.biome.getRegistryName()))
                    .forEach(entry -> table.add(registry.getRegistryName(), entry.biome.getRegistryName(), entry.itemWeight));

            for(@Nonnull final String row : table.build("\n").split("\n")) if(!row.isEmpty()) sender.sendMessage(new TextComponentString(row));
        }

        if(type == null || type.equals("structures")) {
            @Nonnull final TextTable table = new TextTable(Arrays.asList(TextTable.column("Registry"), TextTable.column("Structure")));
            for(@Nonnull final INetherAPIRegistry registry : registries) registry.getStructures().stream()
                    .sorted(Comparator.comparing(INetherAPIStructureEntry::getCommandName))
                    .forEach(entry -> table.add(registry.getRegistryName(), entry.getCommandName()));

            for(@Nonnull final String row : table.build("\n").split("\n")) if(!row.isEmpty()) sender.sendMessage(new TextComponentString(row));
        }
    }
}
