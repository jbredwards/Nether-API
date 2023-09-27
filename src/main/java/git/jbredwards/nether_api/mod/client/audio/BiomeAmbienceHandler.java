/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.client.audio;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.world.IAmbienceWorldProvider;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
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
import java.util.HashMap;
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
    @Nonnull static final Map<Biome, FadingSound> activeBiomeAmbientSounds = new HashMap<>();
    @Nonnull static final Minecraft mc = Minecraft.getMinecraft();

    @Nullable
    static Biome currentBiome;
    static float caveAmbienceChance;

    @SubscribeEvent
    static void onPlayerTick(@Nonnull TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            if(mc.player != null && mc.world != null) {
                final BlockPos pos = new BlockPos(ActiveRenderInfo.projectViewFromEntity(mc.player, mc.getRenderPartialTicks()));
                final Biome biome = mc.world.getBiome(pos);
                activeBiomeAmbientSounds.values().removeIf(FadingSound::isDonePlaying);

                //continuous biome ambient sound
                if(biome != currentBiome) {
                    currentBiome = biome;
                    activeBiomeAmbientSounds.values().forEach(FadingSound::fadeOut);

                    final SoundEvent ambientSound = IAmbienceWorldProvider.getAmbienceOrFallback(mc.world, pos, biome, SoundEvent.class, IAmbienceWorldProvider::getAmbientSound, IAmbienceBiome::getAmbientSound, null);
                    if(ambientSound != null) activeBiomeAmbientSounds.compute(biome, (biomeIn, sound) -> {
                        if(sound == null) {
                            sound = new FadingSound(mc.player, ambientSound, SoundCategory.AMBIENT);
                            mc.getSoundHandler().playSound(sound);
                        }

                        sound.fadeIn();
                        return sound;
                    });
                }

                //ensure continuous biome ambient sound is continuous
                else if(activeBiomeAmbientSounds.containsKey(biome) && !mc.getSoundHandler().isSoundPlaying(activeBiomeAmbientSounds.get(biome))) {
                    currentBiome = null;
                    activeBiomeAmbientSounds.clear();
                }

                //random biome ambient sound
                final ISoundAmbience ambientSound = IAmbienceWorldProvider.getAmbienceOrFallback(mc.world, pos, biome, ISoundAmbience.class, IAmbienceWorldProvider::getRandomAmbientSound, IAmbienceBiome::getRandomAmbientSound, null);
                if(ambientSound != null && mc.player.getRNG().nextDouble() < ambientSound.getChancePerTick()) {
                    final ISound sound = new PositionedSoundRecord(ambientSound.getSoundEvent().getSoundName(), SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, 0, 0, 0);
                    mc.getSoundHandler().playSound(sound);
                }

                //random dark biome ambient sound
                final IDarkSoundAmbience caveSound = IAmbienceWorldProvider.getAmbienceOrFallback(mc.world, pos, biome, IDarkSoundAmbience.class, IAmbienceWorldProvider::getDarkAmbienceSound, IAmbienceBiome::getDarkAmbienceSound, DarkSoundAmbience.DEFAULT_CAVE);
                if(caveSound != null) {
                    final int searchDiameter = caveSound.getLightSearchRadius() << 1 + 1;

                    final double searchX = mc.player.posX + mc.player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final double searchY = mc.player.posY + mc.player.getEyeHeight() + mc.player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final double searchZ = mc.player.posZ + mc.player.getRNG().nextInt(searchDiameter) - caveSound.getLightSearchRadius();
                    final BlockPos searchPos = new BlockPos(searchX, searchY, searchZ);

                    final int skyLight = mc.world.getLightFor(EnumSkyBlock.SKY, searchPos);
                    caveAmbienceChance -= skyLight > 0 ? skyLight * 0.001 / 15 : (mc.world.getLightFor(EnumSkyBlock.BLOCK, searchPos) - 1) * caveSound.getChancePerTick();
                    if(caveAmbienceChance < 1) caveAmbienceChance = Math.max(caveAmbienceChance, 0);

                    //play the sound
                    else {
                        final double offsetX = searchX - mc.player.posX;
                        final double offsetY = searchY - mc.player.posY - mc.player.getEyeHeight();
                        final double offsetZ = searchZ - mc.player.posZ;
                        final double offset = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                        final double soundOffset = offset * (offset + caveSound.getSoundOffset());

                        final float x = (float)(mc.player.posX + offsetX / soundOffset);
                        final float y = (float)(mc.player.posY + mc.player.getEyeHeight() + offsetY / soundOffset);
                        final float z = (float)(mc.player.posZ + offsetZ / soundOffset);
                        final ISound sound = new PositionedSoundRecord(caveSound.getSoundEvent().getSoundName(), SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, x, y, z);

                        mc.getSoundHandler().playSound(sound);
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
