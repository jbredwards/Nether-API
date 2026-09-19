package git.jbredwards.nether_api.mod.common.world.gen;

import git.jbredwards.nether_api.api.event.BiomeStructureEvent;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBiome;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author jbred
 *
 */
@ApiStatus.Internal
public final class BiomeStructureHandler
{
    @ApiStatus.Internal
    public static void clear() {
        for(@Nonnull final Biome biome : Biome.REGISTRY) {
            ((TransformerBiome.Accessor)biome).nether_api$blocks().clear();
            ((TransformerBiome.Accessor)biome).nether_api$entities().clear();
        }
    }

    @ApiStatus.Internal
    @Nonnull
    public static IBlockState getBlockId(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final IBlockState original) {
        if(original.getBlock() == Blocks.AIR || original.getBlock() == Blocks.MOB_SPAWNER) return original;
        @Nonnull final Map<Pair<String, IBlockState>, IBlockState> blocks = ((TransformerBiome.Accessor)biome).nether_api$blocks();
        @Nonnull final Pair<String, IBlockState> key = Pair.of(structureName, original);

        // Find cached block ID and use that, if it exists.
        @Nullable IBlockState state = blocks.get(key);
        if(state != null) return state;

        // Find value for block ID using event.
        @Nonnull final BiomeStructureEvent.GetBlockID event = new BiomeStructureEvent.GetBlockID(biome, structureName, original);
        try { if(MinecraftForge.TERRAIN_GEN_BUS.post(event)) state = event.replacement; }
        catch(@Nonnull final Throwable e) { NetherAPI.LOGGER.error("Error while finding replacement block for \"{}\" for \"{}\"", original.getBlock().getRegistryName(), structureName, e); }

        // Cache block ID value.
        if(state == null) state = original;
        blocks.put(key, state);
        return state;
    }

    @ApiStatus.Internal
    @Nonnull
    public static List<Biome.SpawnListEntry> prepareSpawnList(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final EnumCreatureType type, @Nonnull final Supplier<List<Biome.SpawnListEntry>> defaultSpawnList) {
        @Nonnull final Map<Pair<String, EnumCreatureType>, List<Biome.SpawnListEntry>> entities = ((TransformerBiome.Accessor)biome).nether_api$entities();
        @Nonnull final Pair<String, EnumCreatureType> key = Pair.of(structureName, type);

        // Find cached entity list and use that, if it exists.
        @Nullable List<Biome.SpawnListEntry> ret = entities.get(key);
        if(ret != null) return ret;

        // Copy default spawn list.
        @Nonnull final List<Biome.SpawnListEntry> original = defaultSpawnList.get();
        ret = new ArrayList<>(original.size());
        for(@Nonnull final Biome.SpawnListEntry entry : original) ret.add(new Biome.SpawnListEntry(entry.entityClass, entry.itemWeight, entry.minGroupCount, entry.maxGroupCount));

        // Modify entity list using event.
        @Nonnull final BiomeStructureEvent.PrepareSpawnList event = new BiomeStructureEvent.PrepareSpawnList(biome, structureName, ret, type);
        try { MinecraftForge.TERRAIN_GEN_BUS.post(event); }
        catch(@Nonnull final Throwable e) { NetherAPI.LOGGER.error("Error while preparing spawn list of type \"{}\" for \"{}\"", type, structureName, e); }

        // Cache entity list.
        entities.put(key, ret);
        return ret;
    }
}
