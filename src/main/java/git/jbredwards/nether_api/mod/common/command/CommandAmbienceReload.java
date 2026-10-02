package git.jbredwards.nether_api.mod.common.command;

import com.google.gson.JsonElement;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.config.ambience.AmbienceConfig;
import git.jbredwards.nether_api.mod.common.network.MessageAmbienceConfig;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public class CommandAmbienceReload extends CommandBase
{
    @Nonnull
    @Override
    public String getName() {
        return "ambience";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull final ICommandSender sender) {
        return "commands." + NetherAPI.MODID + '.' + getName() + ".usage";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args, @Nullable final BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "reload") : Collections.emptyList();
    }

    @Override
    public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args) throws CommandException {
        if(args.length != 1 || !args[0].equals("reload")) throw new WrongUsageException(getUsage(sender));

        @Nullable final JsonElement file = AmbienceConfig.read();
        if(!NetherAPIConfig.ambienceJsonSync && !server.isDedicatedServer()) {
            synchronized(NetherAPI.MODID) { AmbienceConfig.load(file); }
        }
        else {
            if(server.isDedicatedServer()) AmbienceConfig.load(file);
            if(NetherAPIConfig.ambienceJsonSync) NetherAPI.WRAPPER.sendToAll(new MessageAmbienceConfig(file));
        }

        if(file == null) throw new CommandException("commands." + NetherAPI.MODID + '.' + getName() + ".fail");
        notifyCommandListener(sender, this, "commands." + NetherAPI.MODID + '.' + getName() + ".success");
    }

    @Override
    public boolean checkPermission(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender) {
        return sender.canUseCommand(!server.isDedicatedServer() && server.getCurrentPlayerCount() == 1 ? 0 : getRequiredPermissionLevel(), getName());
    }
}
