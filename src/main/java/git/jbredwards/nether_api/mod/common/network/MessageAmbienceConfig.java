package git.jbredwards.nether_api.mod.common.network;

import git.jbredwards.nether_api.mod.client.config.GuiButtonAmbienceReload;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class MessageAmbienceConfig implements IMessage, IMessageHandler<MessageAmbienceConfig, IMessage>
{
    @Override
    public void fromBytes(@Nonnull final ByteBuf buf) {
        // NO-OP
    }

    @Override
    public void toBytes(@Nonnull final ByteBuf buf) {
        // NO-OP
    }

    @Nullable
    @Override
    public IMessage onMessage(@Nonnull final MessageAmbienceConfig message, @Nonnull final MessageContext ctx) {
        if(ctx.side.isClient()) onMessageClient();
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void onMessageClient() {
        Minecraft.getMinecraft().addScheduledTask(GuiButtonAmbienceReload::run);
    }
}
