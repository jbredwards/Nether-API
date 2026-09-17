package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IMusicBiome;
import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@ApiStatus.Internal
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetherAPI.MODID, value = Side.CLIENT)
public final class BiomeMusicHandler
{
    @Nonnull private static final Minecraft mc = Minecraft.getMinecraft();

    @Nullable private static Biome prevBiome;
    @Nullable private static IMusicType prevType;

    @Nullable
    public static MusicTicker.MusicType get(@Nullable final MusicTicker.MusicType defaultMusic) {
        if(defaultMusic != null) {
            resetCurrentMusicType();
            return defaultMusic;
        }

        if(prevType != null && !mc.getSoundHandler().isSoundPlaying(mc.getMusicTicker().currentMusic)) resetCurrentMusicType();
        @Nonnull final Biome biome = mc.world.getBiome(new BlockPos(mc.player.getPositionEyes(mc.getRenderPartialTicks())));
        @Nullable final IMusicType type = biome instanceof IMusicBiome ? getActiveType((IMusicBiome)biome) : null;

        if(prevType == null || type.replacesCurrentMusic(prevType.getMusicType()) || prevBiome != biome && prevType.isBiomeLocal()) prevType = type;
        prevBiome = biome;
        return prevType != null ? prevType.getMusicType() : null;
    }

    @Nonnull
    private static IMusicType getActiveType(@Nonnull final IMusicBiome biome) {
        if(mc.ingameGUI.getBossOverlay().shouldPlayEndBossMusic()) return biome.getBossMusicType();
        else if(mc.player != null && mc.player.capabilities.isCreativeMode && mc.player.capabilities.allowFlying) return biome.getCreativeMusicType();
        else return biome.getMusicType();
    }

    private static void resetCurrentMusicType() {
        prevType = null;
        prevBiome = null;
    }

    @SubscribeEvent
    static void resetCurrentMusicType(@Nonnull final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START && prevType != null && mc.player == null) resetCurrentMusicType();
    }

    @Deprecated
    @Nonnull
    static MusicTicker.MusicType get(@Nonnull final IMusicBiome music) {
        @Nullable final MusicTicker.MusicType type = get((MusicTicker.MusicType)null);
        return type != null ? type : getActiveType(music).getMusicType();
    }
}
