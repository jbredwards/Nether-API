package git.jbredwards.nether_api.api.event;

import git.jbredwards.nether_api.mod.common.world.gen.BiomeStructureHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * BiomeStructureEvent is fired whenever an event involving biome-based structures occurs.<br>
 * If a method utilizes this {@link net.minecraftforge.fml.common.eventhandler.Event Event} as its parameter,
 * the method will receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#TERRAIN_GEN_BUS}
 * unless stated otherwise in their Javadocs.
 * <br>
 * <br>
 * @author jbred
 **/
@ApiStatus.AvailableSince("1.5.0")
public abstract class BiomeStructureEvent extends BiomeEvent
{
    @ApiStatus.AvailableSince("1.5.0") @Nonnull public final String structureName;
    @ApiStatus.AvailableSince("1.5.0")
    public BiomeStructureEvent(@Nonnull final Biome biome, @Nonnull final String structureName) {
        super(biome);
        this.structureName = structureName;
    }

    /**
     * Fires the {@link GetBlockID} event if {@code replacement} isn't cached internally.
     * Returns {@code original} if the block is air, a monster spawner, or if the event encounters an error.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static IBlockState getBlockId(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final IBlockState original) {
        return BiomeStructureHandler.getBlockId(biome, structureName, original);
    }

    /**
     * Fires the {@link PrepareSpawnList} event its result isn't cached internally.
     * @throws NullPointerException If any parameters are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static List<Biome.SpawnListEntry> prepareSpawnList(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final EnumCreatureType type, @Nonnull final Supplier<List<Biome.SpawnListEntry>> defaultSpawnList) {
        return BiomeStructureHandler.prepareSpawnList(biome, structureName, type, defaultSpawnList);
    }

    /**
     * This event is fired when the structure generator attempts to choose a block ID based on its biome.
     * Currently, the only Vanilla structure that supports this event is "Fortress". It's generally recommended
     * for modded structures registered using Nether API to use this event, but it's not required.<br>
     * <br>
     * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * Cancelling this event will cause the replacement to be applied.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
     * <br>
     * <br>
     * @see net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID
     * @author jbred
     *
     */
    @Cancelable
    @ApiStatus.AvailableSince("1.5.0")
    public static class GetBlockID extends BiomeStructureEvent
    {
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final IBlockState original;
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public IBlockState replacement;
        @ApiStatus.AvailableSince("1.5.0")
        public GetBlockID(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final IBlockState original) {
            super(biome, structureName);
            this.original = original;
            this.replacement = original;
        }

        /**
         * Sets the replacement block ID and cancels this event.
         * @author jbred
         */
        @ApiStatus.AvailableSince("1.5.0")
        public void setReplacement(@Nonnull final IBlockState replacement) {
            this.replacement = replacement;
            this.setCanceled(true);
        }
    }

    /**
     * This event is fired when the structure generator attempts to choose an entity ID (for monster spawners) based on its biome.
     * Currently, the only Vanilla structure that supports this event is "Fortress". It's generally recommended
     * for modded structures registered using Nether API to use this event, but it's not required.<br>
     * <br>
     * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * Cancelling this event will prevent the original entity ID from being applied.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
     * <br>
     * <br>
     * @author jbred
     *
     */
    @Cancelable
    @ApiStatus.AvailableSince("1.5.0")
    public static class PrepareSpawnerLogic extends BiomeStructureEvent
    {
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final MobSpawnerBaseLogic spawnerLogic;
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final ResourceLocation original;
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final Random random;
        @ApiStatus.AvailableSince("1.5.0")
        public PrepareSpawnerLogic(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final MobSpawnerBaseLogic spawnerLogic, @Nonnull final ResourceLocation original, @Nonnull final Random random) {
            super(biome, structureName);
            this.spawnerLogic = spawnerLogic;
            this.original = original;
            this.random = random;
        }
    }

    /**
     * This event is fired when the structure generator attempts to create the list of naturally spawning entities based on its biome.
     * Currently, the only Vanilla structure that supports this event is "Fortress". It's generally recommended
     * for modded structures registered using Nether API to use this event, but it's not required.<br>
     * <br>
     * This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
     * Cancelling this event has no effect on the spawn list.
     * <br>
     * This event does not have a {@link HasResult HasResult}.
     * <br>
     * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
     * <br>
     * <br>
     * @see git.jbredwards.nether_api.api.structure.ISpawningStructure
     * @author jbred
     *
     */
    @Cancelable
    @ApiStatus.AvailableSince("1.5.0")
    public static class PrepareSpawnList extends BiomeStructureEvent
    {
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final List<Biome.SpawnListEntry> spawnList;
        @ApiStatus.AvailableSince("1.5.0") @Nonnull public final EnumCreatureType spawnListType;
        @ApiStatus.AvailableSince("1.5.0")
        public PrepareSpawnList(@Nonnull final Biome biome, @Nonnull final String structureName, @Nonnull final List<Biome.SpawnListEntry> spawnList, @Nonnull final EnumCreatureType spawnListType) {
            super(biome, structureName);
            this.spawnList = spawnList;
            this.spawnListType = spawnListType;
        }
    }
}
