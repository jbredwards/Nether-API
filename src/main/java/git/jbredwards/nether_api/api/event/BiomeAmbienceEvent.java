/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when getting biome ambience, see {@link git.jbredwards.nether_api.api.biome.IAmbienceBiome IAmbienceBiome} for more information.<br>
 * Biome ambience can be any of the following:<br>{@link net.minecraft.util.SoundEvent SoundEvent}, {@link git.jbredwards.nether_api.api.audio.ISoundAmbience ISoundAmbience}, {@link git.jbredwards.nether_api.api.audio.IDarkSoundAmbience IDarkSoundAmbience}, or {@link net.minecraft.client.particle.IParticleFactory IParticleFactory[]} (client-side only).<br>
 * <br>
 * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
 * Cancelling this event will cause its ambience to be used.
 * <br>
 * This event does not have a {@link HasResult HasResult}.
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
 * <br>
 * <br>
 * @since 1.3.0
 * @author jbred
 *
 */
@Cancelable
public class BiomeAmbienceEvent<T> extends GenericEvent<T>
{
    @Nonnull public final Biome biome;
    @Nonnull public final World world;
    @Nonnull public final BlockPos pos;
    @Nullable public T ambience;

    public BiomeAmbienceEvent(@Nonnull Class<T> typeIn, @Nonnull Biome biomeIn, @Nonnull World worldIn, @Nonnull BlockPos posIn) {
        super(typeIn);
        biome = biomeIn;
        world = worldIn;
        pos = posIn;
    }
}
