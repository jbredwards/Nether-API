/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
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
