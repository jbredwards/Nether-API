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

package git.jbredwards.nether_api.api.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import org.jetbrains.annotations.ApiStatus;

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
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.3.0")
@Cancelable
public class BiomeAmbienceEvent<T> extends GenericEvent<T>
{
    @ApiStatus.AvailableSince("1.3.0") @Nonnull public final Biome biome;
    @ApiStatus.AvailableSince("1.3.0") @Nonnull public final World world;
    @ApiStatus.AvailableSince("1.3.0") @Nonnull public final BlockPos pos;
    @ApiStatus.AvailableSince("1.3.0") @Nullable public T ambience;
    @ApiStatus.AvailableSince("1.3.0")
    public BiomeAmbienceEvent(@Nonnull final Class<T> typeIn, @Nonnull final Biome biomeIn, @Nonnull final World worldIn, @Nonnull final BlockPos posIn) {
        super(typeIn);
        this.biome = biomeIn;
        this.world = worldIn;
        this.pos = posIn;
    }
}
