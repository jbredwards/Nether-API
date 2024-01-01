/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.event;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

import javax.annotation.Nonnull;

/**
 * Fired when determining the GenLayers used for biome generation in the Nether or End.<br>
 * <br>
 * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
 * <br>
 * This event does not have a {@link HasResult HasResult}.
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
 * <br>
 * <br>
 * @since 1.0.0
 * @author jbred
 *
 */
public abstract class NetherAPIInitBiomeGensEvent extends WorldTypeEvent
{
    @Nonnull
    public GenLayer[] biomeGenerators;
    public final long seed;

    public NetherAPIInitBiomeGensEvent(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
        super(worldTypeIn);
        seed = seedIn;
        biomeGenerators = originalGeneratorsIn;
    }

    /**
     * Fired when determining the GenLayers used for biome generation in the End.<br>
     * <br>
     * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
     * <br>
     * <br>
     * @since 1.0.0
     * @author jbred
     *
     */
    public static class End extends NetherAPIInitBiomeGensEvent
    {
        public End(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
            super(worldTypeIn, seedIn, originalGeneratorsIn);
        }
    }

    /**
     * Fired when determining the GenLayers used for biome generation in the Nether.<br>
     * <br>
     * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
     * <br>
     * <br>
     * @since 1.0.0
     * @author jbred
     *
     */
    public static class Nether extends NetherAPIInitBiomeGensEvent
    {
        public Nether(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
            super(worldTypeIn, seedIn, originalGeneratorsIn);
        }
    }
}
