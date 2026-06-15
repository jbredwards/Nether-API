package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IMusicBiome;
import git.jbredwards.nether_api.api.audio.IMusicType;
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
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Currently only supports the Nether and End.<br>
 * TODO: Support all dimensions.
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
    @Nullable private static Integer prevDimension;

    @Nonnull
    public static MusicTicker.MusicType get(@Nonnull final DimensionType dimension, @Nonnull final IMusicBiome defaultMusic) {
        if(mc.player == null || mc.player.dimension != dimension.getId()) return getActiveType(defaultMusic).getMusicType();

        if(prevType != null && !mc.getSoundHandler().isSoundPlaying(mc.getMusicTicker().currentMusic)) prevType = null;
        prevDimension = dimension.getId();

        @Nonnull final Biome biome = mc.world.getBiome(new BlockPos(mc.player.getPositionEyes(mc.getRenderPartialTicks())));
        @Nonnull final IMusicType type = getActiveType(biome instanceof IMusicBiome ? (IMusicBiome)biome : defaultMusic);

        if(prevType == null || type.replacesCurrentMusic(prevType.getMusicType()) || prevBiome != biome && prevType.isBiomeLocal()) prevType = type;
        prevBiome = biome;

        return prevType.getMusicType();
    }

    @Nonnull
    private static IMusicType getActiveType(@Nonnull final IMusicBiome biome) {
        if(mc.ingameGUI.getBossOverlay().shouldPlayEndBossMusic()) return biome.getBossMusicType();
        else if(mc.player != null && mc.player.capabilities.isCreativeMode && mc.player.capabilities.allowFlying) return biome.getCreativeMusicType();
        else return biome.getMusicType();
    }

    private static void tick() {
        if(prevType != null && (mc.player == null || prevDimension != null && mc.player.dimension != prevDimension)) {
            prevType = null;
            prevDimension = null;
        }
    }

    @SubscribeEvent
    static void resetCurrentMusicType(@Nonnull final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) tick();
    }
}
