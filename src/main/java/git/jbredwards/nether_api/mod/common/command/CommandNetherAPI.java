/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.command;

import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CommandNetherAPI extends CommandTreeBase
{
    public CommandNetherAPI() {
        super.addSubcommand(new CommandListInfo());
        super.addSubcommand(new CommandTreeHelp(this));
    }

    @Nonnull
    @Override
    public String getName() { return NetherAPI.MODID; }

    @Nonnull
    @Override
    public String getUsage(@Nonnull final ICommandSender sender) { return "commands." + NetherAPI.MODID + '.' + getName() + ".usage"; }

    @Override
    public void addSubcommand(@Nonnull final ICommand command) {
        throw new UnsupportedOperationException(String.format("Don't add sub-commands to /%s, create your own command.", getName()));
    }
}
