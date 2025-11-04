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

import net.minecraft.client.audio.MusicTicker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Allows End and Nether biomes to have their own music, used by
 * {@link git.jbredwards.nether_api.api.biome.IEndBiome IEndBiome} and
 * {@link git.jbredwards.nether_api.api.biome.INetherBiome INetherBiome}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IMusicType
{
    /**
     * Use {@link net.minecraftforge.client.EnumHelperClient#addMusicType EnumHelperClient::addMusicType} to create a custom MusicType.
     * The result of that method should be saved to a static final field in your client proxy, and that field is what should be
     * returned by this method.
     *
     * @return The MusicType that gets played.
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    MusicTicker.MusicType getMusicType();

    /**
     * @return Whether this should replace the currently playing music.
     */
    @SideOnly(Side.CLIENT)
    boolean replacesCurrentMusic(@Nonnull final MusicTicker.MusicType currentlyPlaying);

    /**
     * @return Whether this should only play while the player remains within the same biome.
     * @since 1.4.0
     */
    default boolean isBiomeLocal() { return false; }
}
