/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
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
public class MessageTeleportFX implements IMessage
{
    public double x, y, z, prevX, prevY, prevZ;
    public float width, height;
    public boolean valid;

    public MessageTeleportFX() {}
    public MessageTeleportFX(@Nonnull final Entity entityIn, final double prevXIn, final double prevYIn, final double prevZIn) {
        valid = true;
        x = entityIn.posX;
        y = entityIn.posY;
        z = entityIn.posZ;
        prevX = prevXIn;
        prevY = prevYIn;
        prevZ = prevZIn;
        width = entityIn.width;
        height = entityIn.height;
    }

    @Override
    public void fromBytes(@Nonnull final ByteBuf buf) {
        //noinspection AssignmentUsedAsCondition
        if(valid = buf.readBoolean()) {
            x = buf.readDouble();
            y = buf.readDouble();
            z = buf.readDouble();
            prevX = buf.readDouble();
            prevY = buf.readDouble();
            prevZ = buf.readDouble();
            width = buf.readFloat();
            height = buf.readFloat();
        }
    }

    @Override
    public void toBytes(@Nonnull final ByteBuf buf) {
        buf.writeBoolean(valid);
        if(valid) buf.writeDouble(x).writeDouble(y).writeDouble(z)
                .writeDouble(prevX).writeDouble(prevY).writeDouble(prevZ)
                .writeFloat(width).writeFloat(height);
    }

    public enum Handler implements IMessageHandler<MessageTeleportFX, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull final MessageTeleportFX msg, @Nonnull final MessageContext ctx) {
            if(msg.valid && ctx.side.isClient()) onMessageClient(msg);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void onMessageClient(@Nonnull final MessageTeleportFX msg) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final double scale = (double)1 / 127;
                for(double slide = 0; slide <= 1; slide += scale) Minecraft.getMinecraft().renderGlobal.spawnParticle(
                        EnumParticleTypes.PORTAL,
                        msg.prevX + (msg.x - msg.prevX) * slide + (Math.random() - 0.5) * msg.width * 2,
                        msg.prevY + (msg.y - msg.prevY) * slide + Math.random() * msg.height,
                        msg.prevZ + (msg.z - msg.prevZ) * slide + (Math.random() - 0.5) * msg.width * 2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                );
            });
        }
    }
}
