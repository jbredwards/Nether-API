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
