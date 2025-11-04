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

package git.jbredwards.nether_api.api.structure;

import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Used to register structures that can make use of {@link INetherAPIChunkGenerator} instances.
 *
 * @since 1.3.0
 * @author jbred
 *
 */
public interface INetherAPIStructureEntry
{
    /**
     * @return the name of the structure used by the /locate command (should match the result of calling the structure's {@link MapGenStructure#getStructureName()} method).
     */
    @Nonnull
    String getCommandName();

    /**
     * @return the structure factory associated with this entry.
     */
    @Nonnull
    Function<INetherAPIChunkGenerator, MapGenStructure> getStructureFactory();
}
