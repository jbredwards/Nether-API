package git.jbredwards.nether_api.mod.common.network;

import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.config.ambience.AmbienceConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author jbred
 *
 */
public class MessageAmbienceConfig implements IMessage, IMessageHandler<MessageAmbienceConfig, IMessage>
{
    @Nullable
    public JsonElement file;

    public MessageAmbienceConfig() {}
    public MessageAmbienceConfig(@Nullable final JsonElement fileIn) {
        file = fileIn;
    }

    @Override
    public void fromBytes(@Nonnull final ByteBuf buf) {
        if(buf.readBoolean()) {
            @Nonnull final ByteBufInputStream is = new ByteBufInputStream(buf, buf.readInt());
            try(@Nonnull final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                file = TypeAdapters.JSON_ELEMENT.fromJson(reader);
            }

            catch(@Nonnull final IOException e) { throw new RuntimeException(e); } // Unpossible?
        }
    }

    @Override
    public void toBytes(@Nonnull final ByteBuf buf) {
        if(file == null) buf.writeBoolean(false);
        else {
            buf.writeBoolean(true);
            final int startIndex = buf.writerIndex();

            @Nonnull final ByteBufOutputStream os = new ByteBufOutputStream(buf.writeInt(0)); // Allocate size int.
            try(@Nonnull final OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                TypeAdapters.JSON_ELEMENT.toJson(writer, file);
            }

            catch(@Nonnull final IOException e) { throw new RuntimeException(e); } // Unpossible?
            buf.setInt(startIndex, buf.writerIndex() - startIndex - 4); // Write size int to start index.
        }
    }

    @Nullable
    @Override
    public IMessage onMessage(@Nonnull final MessageAmbienceConfig message, @Nonnull final MessageContext ctx) {
        if(ctx.side.isClient() && NetherAPIConfig.ambienceJsonSync) onMessageClient(message.file);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static void onMessageClient(@Nullable final JsonElement file) {
        Minecraft.getMinecraft().addScheduledTask(() -> AmbienceConfig.load(file));
    }
}
