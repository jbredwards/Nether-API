package git.jbredwards.nether_api.api.audio;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Having your biome class implement this allows it to have custom music.
 *
 * @see git.jbredwards.nether_api.api.biome.IEndBiome
 * @see git.jbredwards.nether_api.api.biome.INetherBiome
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public interface IMusicBiome
{
    /**
     * @return The ambient music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    IMusicType getMusicType();

    /**
     * @return The boss music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    IMusicType getBossMusicType();

    /**
     * @return The creative mode music that plays while players are in this biome.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    IMusicType getCreativeMusicType();

    /**
     * @return A {@code IMusicBiome} instance that uses the provided music types.
     * @throws NullPointerException If ambientType or bossType are null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static IMusicBiome of(@Nonnull final IMusicType survivalType, @Nonnull final IMusicType bossType, @Nonnull final IMusicType creativeType) {
        Objects.requireNonNull(survivalType);
        Objects.requireNonNull(bossType);
        Objects.requireNonNull(creativeType);
        return new IMusicBiome() {
            @Nonnull
            @SideOnly(Side.CLIENT)
            @Override
            public IMusicType getMusicType() {
                return survivalType;
            }

            @Nonnull
            @SideOnly(Side.CLIENT)
            @Override
            public IMusicType getBossMusicType() {
                return bossType;
            }

            @Nonnull
            @SideOnly(Side.CLIENT)
            @Override
            public IMusicType getCreativeMusicType() {
                return creativeType;
            }
        };
    }

    /**
     * @return A {@code IMusicBiome} instance that uses one music type.
     * @throws NullPointerException If type is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static IMusicBiome of(@Nonnull final IMusicType type) {
        return of(type, type, type);
    }

    /**
     * @return A {@code IMusicBiome} instance that shares its creative and survival music.
     * @throws NullPointerException If type is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static IMusicBiome of(@Nonnull final IMusicType survivalType, @Nonnull final IMusicType bossType) {
        return of(survivalType, bossType, survivalType);
    }
}
