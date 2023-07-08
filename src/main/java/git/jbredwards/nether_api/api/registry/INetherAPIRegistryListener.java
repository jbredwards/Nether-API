/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.api.registry;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

/**
 * Biomes or MapGenStructures should implement this if they update when they are either added, or removed, from an INetherAPIRegistry.
 *
 * @since 1.1.0
 * @author jbred
 *
 */
public interface INetherAPIRegistryListener
{
    /**
     * Called when this is added to an INetherAPIRegistry.
     */
    default void onAddedToRegistry(@Nonnull INetherAPIRegistry registry, @Nonnull OptionalInt newWeight) {}

    /**
     * Called when this is removed from an INetherAPIRegistry.
     */
    default void onRemovedFromRegistry(@Nonnull INetherAPIRegistry registry, @Nonnull OptionalInt oldWeight) {}
}
