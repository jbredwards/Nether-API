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
import net.minecraftforge.event.terraingen.WorldTypeEvent;

import javax.annotation.Nonnull;

/**
 * Fired when determining the size of the biomes to generate for the Nether or End.<br>
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
public abstract class NetherAPIBiomeSizeEvent extends WorldTypeEvent
{
    public int biomeSize;
    public NetherAPIBiomeSizeEvent(@Nonnull WorldType worldTypeIn, int biomeSizeIn) {
        super(worldTypeIn);
        biomeSize = biomeSizeIn;
    }

    /**
     * Fired when determining the size of the biomes to generate for the End.<br>
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
    public static class End extends NetherAPIBiomeSizeEvent
    {
        public End(@Nonnull WorldType worldTypeIn, int biomeSizeIn) { super(worldTypeIn, biomeSizeIn); }
    }

    /**
     * Fired when determining the size of the biomes to generate for the Nether.<br>
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
    public static class Nether extends NetherAPIBiomeSizeEvent
    {
        public Nether(@Nonnull WorldType worldTypeIn, int biomeSizeIn) { super(worldTypeIn, biomeSizeIn); }
    }
}
