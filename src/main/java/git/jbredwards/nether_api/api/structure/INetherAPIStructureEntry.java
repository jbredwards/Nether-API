/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
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
