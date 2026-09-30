package git.jbredwards.nether_api.mod.common.command;

import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.client.config.GuiButtonAmbienceReload;
import git.jbredwards.nether_api.mod.common.config.ambience.AmbienceConfig;
import git.jbredwards.nether_api.mod.common.network.MessageAmbienceConfig;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

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
        boolean reloadOnServer = true;
        if(sender instanceof EntityPlayerMP) {
            // Reload on server & client (if it's integrated server).
            if(FMLLaunchHandler.side().isClient() && ((EntityPlayerMP)sender).connection.netManager.isLocalChannel()) {
                reloadOnServer = false;
                synchronized(NetherAPI.MODID) { GuiButtonAmbienceReload.run(); }
            }
            // Schedule client to reload.
            else NetherAPI.WRAPPER.sendTo(new MessageAmbienceConfig(), (EntityPlayerMP)sender);
        }

        // Not a console or server invoking the command, no-op.
        else if(!(sender instanceof RConConsoleSource) && !(sender instanceof MinecraftServer)) {
            throw new PlayerNotFoundException("commands." + NetherAPI.MODID + '.' + getName() + ".fail");
        }

        // Reload on server.
        if(reloadOnServer) AmbienceConfig.load();
        notifyCommandListener(sender, this, "commands." + NetherAPI.MODID + '.' + getName() + ".success");
    }

    @Override
    public boolean checkPermission(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender) {
        return sender.canUseCommand(!server.isDedicatedServer() && server.getCurrentPlayerCount() == 1 ? 0 : getRequiredPermissionLevel(), getName());
    }
}
