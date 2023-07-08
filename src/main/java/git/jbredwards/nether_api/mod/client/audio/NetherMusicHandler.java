/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.biome.INetherBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetherAPI.MODID, value = Side.CLIENT)
public final class NetherMusicHandler
{
    @Nonnull static Minecraft mc = Minecraft.getMinecraft();
    @Nullable static MusicTicker.MusicType currentType;

    @Nullable
    public static MusicTicker.MusicType getMusicType() {
        if(currentType != null && !mc.getSoundHandler().isSoundPlaying(mc.getMusicTicker().currentMusic)) currentType = null;
        final Biome biome = Minecraft.getMinecraft().world.getBiome(new BlockPos(ActiveRenderInfo.getCameraPosition()));
        if(biome instanceof INetherBiome) {
            final IMusicType musicType = ((INetherBiome)biome).getMusicType();
            if(currentType == null) currentType = musicType.getMusicType();
            else if(musicType.replacesCurrentMusic(currentType)) currentType = musicType.getMusicType();
        }

        else if(currentType == null) currentType = MusicTicker.MusicType.NETHER;
        return currentType;
    }

    @SubscribeEvent
    static void resetCurrentMusicType(@Nonnull TickEvent.ClientTickEvent event) {
        if(currentType != null && (mc.player == null || mc.player.dimension != DimensionType.NETHER.getId())) currentType = null;
    }
}
