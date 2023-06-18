package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetherAPI.MODID, value = Side.CLIENT)
final class BiomeAmbienceHandler
{
    @Nonnull
    static final Map<Biome, FadingSound> activeBiomeAmbientSounds = new Object2ObjectArrayMap<>();

    @Nullable
    static Biome currentBiome;
    static float caveAmbienceChance;

    @SubscribeEvent
    static void onPlayerTick(@Nonnull TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            if(player != null && player.world != null) {
                final Biome biome = player.world.getBiome(new BlockPos(ActiveRenderInfo.getCameraPosition()));
                activeBiomeAmbientSounds.values().removeIf(FadingSound::isDonePlaying);

                //continuous biome ambient sound
                if(biome != currentBiome) {
                    currentBiome = biome;
                    activeBiomeAmbientSounds.values().forEach(FadingSound::fadeOut);
                    if(biome instanceof IAmbienceBiome) {
                        final SoundEvent ambientSound = ((IAmbienceBiome)biome).getAmbientSound();
                        if(ambientSound != null) activeBiomeAmbientSounds.compute(biome, (biomeIn, sound) -> {
                            if(sound == null) {
                                sound = new FadingSound(ambientSound, SoundCategory.AMBIENT);
                                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                            }

                            sound.fadeIn();
                            return sound;
                        });
                    }
                }

                //random biome ambient sound
                if(biome instanceof IAmbienceBiome) {
                    final ISoundAmbience ambientSound = ((IAmbienceBiome)biome).getRandomAmbientSound();
                    if(ambientSound != null && player.world.rand.nextDouble() < ambientSound.getChancePerTick()) {
                        final ISound sound = new PositionedSoundRecord(ambientSound.getSoundEvent().getSoundName(), SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, 0, 0, 0);
                        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                    }
                }

                //random dark biome ambient sound
                final IDarkSoundAmbience caveSound = biome instanceof IAmbienceBiome ? ((IAmbienceBiome)biome).getDarkAmbienceSound() : DarkSoundAmbience.DEFAULT_CAVE;
                if(caveSound != null) {
                    final int searchDiameter = caveSound.getLightSearchRadius() << 1 + 1;

                    final double searchX = player.posX + player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final double searchY = player.posY + player.getEyeHeight() + player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final double searchZ = player.posZ + player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final BlockPos searchPos = new BlockPos(searchX, searchY, searchZ);

                    final int skyLight = player.world.getLightFor(EnumSkyBlock.SKY, searchPos);
                    caveAmbienceChance -= skyLight > 0 ? skyLight * 0.001 / 15 : (player.world.getLightFor(EnumSkyBlock.BLOCK, searchPos) - 1) * caveSound.getChancePerTick();
                    if(caveAmbienceChance < 1) caveAmbienceChance = Math.max(caveAmbienceChance, 0);

                    //play the sound
                    else {
                        final double offsetX = searchX - player.posX;
                        final double offsetY = searchY - player.posY - player.getEyeHeight();
                        final double offsetZ = searchZ - player.posZ;
                        final double offset = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                        final double soundOffset = offset * (offset + caveSound.getSoundOffset());

                        final float x = (float)(player.posX + offsetX / soundOffset);
                        final float y = (float)(player.posY + player.getEyeHeight() + offsetY / soundOffset);
                        final float z = (float)(player.posZ + offsetZ / soundOffset);
                        final ISound sound = new PositionedSoundRecord(caveSound.getSoundEvent().getSoundName(), SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, x, y, z);

                        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                        caveAmbienceChance = 0;
                    }
                }
            }

            //this is not active, reset values
            else {
                activeBiomeAmbientSounds.clear();
                caveAmbienceChance = 0;
                currentBiome = null;
            }
        }
    }
}
