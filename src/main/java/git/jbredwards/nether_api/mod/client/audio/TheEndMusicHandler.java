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

package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.audio.impl.VanillaMusicType;
import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
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
public final class TheEndMusicHandler
{
    @Nonnull static final Minecraft mc = Minecraft.getMinecraft();

    @Nullable static Biome prevBiome;
    @Nullable static IMusicType prevType;

    @Nonnull
    public static MusicTicker.MusicType getMusicType() {
        if(prevType != null && !mc.getSoundHandler().isSoundPlaying(mc.getMusicTicker().currentMusic)) prevType = null;

        @Nonnull final Biome biome = mc.world.getBiome(new BlockPos(mc.player.getPositionEyes(mc.getRenderPartialTicks())));
        @Nonnull final IMusicType type;

        if(biome instanceof IEndBiome) type = mc.ingameGUI.getBossOverlay().shouldPlayEndBossMusic() ? ((IEndBiome)biome).getBossMusicType() : ((IEndBiome)biome).getMusicType();
        else type = new VanillaMusicType(mc.ingameGUI.getBossOverlay().shouldPlayEndBossMusic() ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END);

        if(prevType == null || type.replacesCurrentMusic(prevType.getMusicType()) || prevBiome != biome && prevType.isBiomeLocal()) prevType = type;
        prevBiome = biome;

        return prevType.getMusicType();
    }

    @SubscribeEvent
    static void resetCurrentMusicType(@Nonnull TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START && prevType != null && (mc.player == null || mc.player.dimension != DimensionType.THE_END.getId())) prevType = null;
    }
}
