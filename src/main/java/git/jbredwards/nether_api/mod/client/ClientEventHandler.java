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

package git.jbredwards.nether_api.mod.client;

import git.jbredwards.nether_api.api.biome.ILavaTintBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = NetherAPI.MODID, value = Side.CLIENT)
final class ClientEventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void applyLavaColors(@Nonnull ColorHandlerEvent.Block event) {
        final BiomeColorHelper.ColorResolver resolver = (biome, pos) -> biome instanceof ILavaTintBiome ? ((ILavaTintBiome)biome).getBiomeLavaColor(pos) : -1;
        final IBlockColor colorHandler = (state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColorHelper.getColorAtPos(world, pos, resolver) : -1;

        event.getBlockColors().registerBlockColorHandler(colorHandler, Blocks.FLOWING_LAVA, Blocks.LAVA);
    }
}
