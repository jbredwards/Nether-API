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

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Fired when getting the fog color for the Nether or End.<br>
 * <br>
 * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
 * Cancelling this event will cause its fog colors to be used.
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
@SideOnly(Side.CLIENT)
public abstract class NetherAPIFogColorEvent extends Event
{
    @ApiStatus.AvailableSince("1.3.0") @Nonnull public final Biome biome;
    @ApiStatus.AvailableSince("1.3.0") @Nonnull public final World world;
    @ApiStatus.AvailableSince("1.3.0") public final float celestialAngle;
    @ApiStatus.AvailableSince("1.3.0") public final float partialTicks;
    @ApiStatus.AvailableSince("1.3.0") public double fogR, fogG, fogB; // RGB values (must be between 0 and 1)
    @ApiStatus.AvailableSince("1.3.0")
    protected NetherAPIFogColorEvent(@Nonnull final Biome biomeIn, @Nonnull final World worldIn, final float celestialAngleIn, final float partialTicksIn) {
        biome = biomeIn;
        world = worldIn;
        celestialAngle = celestialAngleIn;
        partialTicks = partialTicksIn;
    }

    /**
     * Fired when getting the fog color for the End.<br>
     * <br>
     * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * Cancelling this event will cause its fog colors to be used.
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
    @SideOnly(Side.CLIENT)
    public static class End extends NetherAPIFogColorEvent
    {
        @ApiStatus.AvailableSince("1.3.0")
        public End(@Nonnull final Biome biomeIn, @Nonnull final World worldIn, final float celestialAngleIn, final float partialTicksIn) {
            super(biomeIn, worldIn, celestialAngleIn, partialTicksIn);
        }
    }

    /**
     * Fired when getting the fog color for the Nether.<br>
     * <br>
     * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * Cancelling this event will cause its fog colors to be used.
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
    @SideOnly(Side.CLIENT)
    public static class Nether extends NetherAPIFogColorEvent
    {
        @ApiStatus.AvailableSince("1.3.0")
        public Nether(@Nonnull final Biome biomeIn, @Nonnull final World worldIn, final float celestialAngleIn, final float partialTicksIn) {
            super(biomeIn, worldIn, celestialAngleIn, partialTicksIn);
        }
    }
}
