/*
 * Copyright (c) 2023-2024. jbredwards
 * All rights reserved.
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
