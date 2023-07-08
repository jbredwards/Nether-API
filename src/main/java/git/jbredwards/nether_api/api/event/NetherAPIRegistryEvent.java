/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.event;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

/**
 * Fired when Nether API is ready to accept generation registries.<br>
 * <br>
 * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
 * <br>
 * This event does not have a {@link HasResult HasResult}.
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
 * <br>
 * <br>
 * @since 1.0.0
 * @author jbred
 *
 */
public class NetherAPIRegistryEvent extends Event
{
    /**
     * This is specific to the dimension.
     */
    @Nonnull public final INetherAPIRegistry registry;
    @Nonnull public final MinecraftServer server;

    public NetherAPIRegistryEvent(@Nonnull INetherAPIRegistry registryIn, @Nonnull MinecraftServer serverIn) {
        registry = registryIn;
        server = serverIn;
    }

    /**
     * Fired when Nether API end registry is ready to accept generation registries (WIP).<br>
     * <br>
     * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
     * <br>
     * <br>
     * @since 1.0.0
     * @author jbred
     *
     */
    public static class End extends NetherAPIRegistryEvent
    {
        public End(@Nonnull INetherAPIRegistry registryIn, @Nonnull MinecraftServer serverIn) {
            super(registryIn, serverIn);
        }
    }

    /**
     * Fired when Nether API nether registry is ready to accept generation registries.<br>
     * <br>
     * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS MinecraftForge.EVENT_BUS}.
     * <br>
     * <br>
     * @since 1.0.0
     * @author jbred
     *
     */
    public static class Nether extends NetherAPIRegistryEvent
    {
        public Nether(@Nonnull INetherAPIRegistry registryIn, @Nonnull MinecraftServer serverIn) {
            super(registryIn, serverIn);
        }
    }
}
