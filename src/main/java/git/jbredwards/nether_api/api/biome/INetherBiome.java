package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.audio.impl.VanillaMusicType;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Having your nether biome class implement this is heavily recommended, but not required.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherBiome
{
    @Nonnull
    IBlockState getTopBlock();

    @Nonnull
    IBlockState getFillerBlock();

    /**
     * At the given x and z positions, this replaces "stateToFill" with the provided top and filler blocks.
     */
    default void buildSurface(@Nonnull World world, @Nonnull Random rand, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer, int x, int z, double[] soulSandNoise, double[] gravelNoise, double[] depthBuffer) {
        NetherGenerationUtils.buildSurfaceAndSoulSandGravel(world, rand, primer, x, z, soulSandNoise, gravelNoise, depthBuffer, Blocks.NETHERRACK.getDefaultState(), getTopBlock(), getFillerBlock());
    }

    /**
     * Called instead of vanilla's {@link net.minecraft.world.biome.Biome#decorate(World, Random, BlockPos) Biome::decorate} method.
     */
    default void decorate(@Nonnull IChunkGenerator chunkGenerator, @Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, boolean generateStructures) {
        NetherGenerationUtils.generateVanillaNetherFeatures(world, rand, pos, generateStructures);
    }

    /**
     * @return whether this biome can initiate the generation of Nether Fortresses.
     */
    default boolean canGenerateNetherFortress() { return true; }

    /**
     * @return all possible biomes that can spawn inside this one.
     */
    @Nonnull
    default BiomeManager.BiomeEntry[] getSubBiomes() { return new BiomeManager.BiomeEntry[0]; }

    /**
     * It's also recommended to override {@link net.minecraft.world.biome.Biome#getSkyColorByTemp Biome.getSkyColorByTemp()} for the best result.
     * @return this biome's background fog color.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default Vec3d getFogColor(float celestialAngle, float partialTicks) { return new Vec3d(0.2, 0.3, 0.2); }

    /**
     * @return the ambient music that plays while players are in this biome.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    default IMusicType getMusicType() { return new VanillaMusicType(MusicTicker.MusicType.NETHER); }
}
