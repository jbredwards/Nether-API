/*
 * Copyright (c) 2023-2025. jbredwards
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
        if(event.phase == TickEvent.Phase.END && !mc.isGamePaused()) {
            if(mc.player != null && mc.world != null) {
                final Vec3d posEyes = mc.player.getPositionEyes(mc.getRenderPartialTicks());

                final BlockPos pos = new BlockPos(posEyes);
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
                if(ambientSound != null && Math.random() < ambientSound.getChancePerTick()) {
                    final ISound sound = new PositionedSoundRecord(ambientSound.getSoundEvent().getSoundName(), SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, 0, 0, 0);
                    mc.getSoundHandler().playSound(sound);
                }

                //random dark biome ambient sound
                final IDarkSoundAmbience caveSound = IAmbienceWorldProvider.getAmbienceOrFallback(mc.world, pos, biome, IDarkSoundAmbience.class, IAmbienceWorldProvider::getDarkAmbienceSound, IAmbienceBiome::getDarkAmbienceSound, DarkSoundAmbience.DEFAULT_CAVE);
                if(caveSound != null) {
                    final double searchX = posEyes.x + MathHelper.getInt(mc.player.getRNG(), -caveSound.getLightSearchRadius(), caveSound.getLightSearchRadius());
                    final double searchY = posEyes.y + MathHelper.getInt(mc.player.getRNG(), -caveSound.getLightSearchRadius(), caveSound.getLightSearchRadius());
                    final double searchZ = posEyes.z + MathHelper.getInt(mc.player.getRNG(), -caveSound.getLightSearchRadius(), caveSound.getLightSearchRadius());

                    final BlockPos searchPos = new BlockPos(searchX, searchY, searchZ);
                    final int skyLight = mc.world.getLightFor(EnumSkyBlock.SKY, searchPos);

                    caveAmbienceChance -= skyLight > 0 ? skyLight * 0.001 / 15 : (mc.world.getLightFor(EnumSkyBlock.BLOCK, searchPos) - 1) * caveSound.getChancePerTick();
                    if(caveAmbienceChance < 1) caveAmbienceChance = Math.max(caveAmbienceChance, 0);

                    //play the sound
                    else {
                        final double offsetX = searchX - posEyes.x;
                        final double offsetY = searchY - posEyes.y;
                        final double offsetZ = searchZ - posEyes.z;
                        final double offset = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                        final double soundOffset = offset * (offset + caveSound.getSoundOffset());

                        final float x = (float)(posEyes.x + offsetX / soundOffset);
                        final float y = (float)(posEyes.y + offsetY / soundOffset);
                        final float z = (float)(posEyes.z + offsetZ / soundOffset);
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
