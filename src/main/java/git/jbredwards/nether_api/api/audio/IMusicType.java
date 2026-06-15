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

package git.jbredwards.nether_api.api.audio;

import git.jbredwards.nether_api.api.audio.impl.VanillaMusicType;
import net.minecraft.client.audio.MusicTicker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Allows biomes to have their own music, used by
 * {@link git.jbredwards.nether_api.api.audio.IMusicBiome IMusicBiome}.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface IMusicType
{
    /**
     * Use {@link net.minecraftforge.client.EnumHelperClient#addMusicType EnumHelperClient::addMusicType} to create a custom MusicType.
     * The result of that method should be saved to a static final field in your client proxy, and that field is what should be
     * returned by this method.
     *
     * @return The MusicType that gets played.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    MusicTicker.MusicType getMusicType();

    /**
     * @return Whether this should replace the currently playing music.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @SideOnly(Side.CLIENT)
    boolean replacesCurrentMusic(@Nonnull final MusicTicker.MusicType currentlyPlaying);

    /**
     * @return Whether this should only play while the player remains within the same biome.
     */
    @ApiStatus.AvailableSince("1.4.0")
    default boolean isBiomeLocal() {
        return false;
    }

    /**
     * @return A new {@code IMusicType} instance with Vanilla music properties
     * (cannot replace current music, and is not local to the current biome).
     * @throws NullPointerException If type is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    static VanillaMusicType vanilla(@Nonnull final MusicTicker.MusicType type) {
        return new VanillaMusicType(type);
    }

    /**
     * @return A new {@code IMusicType} instance with custom music properties.
     * @throws NullPointerException If type is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    @SideOnly(Side.CLIENT)
    static VanillaMusicType modded(@Nonnull final MusicTicker.MusicType type, final boolean replacesCurrentMusic, final boolean isBiomeLocal) {
        return new VanillaMusicType(type, replacesCurrentMusic, isBiomeLocal);
    }
}
