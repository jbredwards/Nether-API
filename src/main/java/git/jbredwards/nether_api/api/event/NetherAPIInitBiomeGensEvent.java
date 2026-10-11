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

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import org.jetbrains.annotations.ApiStatus;

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
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public abstract class NetherAPIInitBiomeGensEvent extends WorldTypeEvent
{
    @Nonnull
    @ApiStatus.AvailableSince("1.0.0") public GenLayer[] biomeGenerators;
    @ApiStatus.AvailableSince("1.0.0") public final long seed;
    @ApiStatus.AvailableSince("1.0.0")
    public NetherAPIInitBiomeGensEvent(@Nonnull final WorldType worldTypeIn, final long seedIn, @Nonnull final GenLayer[] originalGeneratorsIn) {
        super(worldTypeIn);
        this.seed = seedIn;
        this.biomeGenerators = originalGeneratorsIn;
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
     * @author jbred
     *
     */
    @ApiStatus.AvailableSince("1.0.0")
    public static class End extends NetherAPIInitBiomeGensEvent
    {
        @ApiStatus.AvailableSince("1.0.0")
        public End(@Nonnull final WorldType worldTypeIn, final long seedIn, @Nonnull final GenLayer[] originalGeneratorsIn) {
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
     * @author jbred
     *
     */
    @ApiStatus.AvailableSince("1.0.0")
    public static class Nether extends NetherAPIInitBiomeGensEvent
    {
        @ApiStatus.AvailableSince("1.0.0")
        public Nether(@Nonnull final WorldType worldTypeIn, final long seedIn, @Nonnull final GenLayer[] originalGeneratorsIn) {
            super(worldTypeIn, seedIn, originalGeneratorsIn);
        }
    }
}
