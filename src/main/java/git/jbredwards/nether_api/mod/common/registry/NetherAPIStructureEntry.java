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

package git.jbredwards.nether_api.mod.common.registry;

import git.jbredwards.nether_api.api.structure.INetherAPIStructureEntry;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public class NetherAPIStructureEntry implements INetherAPIStructureEntry
{
    @Nonnull public final String commandName;
    @Nonnull public final Function<INetherAPIChunkGenerator, MapGenStructure> factory;

    public NetherAPIStructureEntry(@Nonnull String commandNameIn, @Nonnull Function<INetherAPIChunkGenerator, MapGenStructure> factoryIn) {
        commandName = commandNameIn;
        factory = factoryIn;
    }

    @Nonnull
    @Override
    public String getCommandName() { return commandName; }

    @Nonnull
    @Override
    public Function<INetherAPIChunkGenerator, MapGenStructure> getStructureFactory() { return factory; }
}
