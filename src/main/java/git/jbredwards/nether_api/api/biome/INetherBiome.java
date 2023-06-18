package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IMusicType;
import git.jbredwards.nether_api.api.audio.impl.VanillaMusicType;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Having your nether biome class implement this is heavily recommended, but not required.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherBiome
{
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
