/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.event;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
 * @since 1.3.0
 * @author jbred
 *
 */
@Cancelable
@SideOnly(Side.CLIENT)
public abstract class NetherAPIFogColorEvent extends Event
{
    @Nonnull public final Biome biome;
    @Nonnull public final World world;
    public final float celestialAngle;
    public final float partialTicks;

    public double fogR, fogG, fogB; // RGB values (must be between 0 and 1)
    protected NetherAPIFogColorEvent(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn) {
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
     * @since 1.3.0
     * @author jbred
     *
     */
    @Cancelable
    @SideOnly(Side.CLIENT)
    public static class End extends NetherAPIFogColorEvent
    {
        public End(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn) {
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
     * @since 1.3.0
     * @author jbred
     *
     */
    @Cancelable
    @SideOnly(Side.CLIENT)
    public static class Nether extends NetherAPIFogColorEvent
    {
        public Nether(@Nonnull Biome biomeIn, @Nonnull World worldIn, float celestialAngleIn, float partialTicksIn) {
            super(biomeIn, worldIn, celestialAngleIn, partialTicksIn);
        }
    }
}
