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

package git.jbredwards.nether_api.mod.common.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lumien.perfectspawn.handler.AsmHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
@ApiStatus.Internal
public final class PlayerSpawnLogic
{
    /**
     * Allows mods to define a global initial spawn dimension.
     */
    @Nullable public static Integer INITIAL_SPAWN_DIMENSION = null;

    /**
     * Allows mods to define initial spawn dimension logic on a per-player basis.
     */
    @Nonnull public static Function<GameProfile, Integer> INITIAL_SPAWN_PER_PLAYER = profile -> null;
    // Collection of all possible nonnull values that can be returned by the INITIAL_SPAWN_PER_PLAYER function.
    @Nonnull public static IntList INITIAL_SPAWN_PER_PLAYER_POSSIBILITIES = new IntArrayList();

    /**
     * Allows mods to override any dimension's "canRespawnHere", aside from the Overworld.
     */
    @Nonnull public static final Int2BooleanMap RESPAWN_DIMENSIONS = new Int2BooleanOpenHashMap();

    public static boolean canSpawnInDimension(@Nonnull final WorldProvider provider, @Nullable final EntityPlayer player) {
        return canSpawnInDimension(provider, player, true);
    }

    private static boolean canSpawnInDimension(@Nonnull final WorldProvider provider, @Nullable final EntityPlayer player, final boolean checkBuiltinRespawn) {
        final int dimension = provider.getDimension();
        if(dimension == 0) return true;

        // Per-player initial spawn dimension override.
        else if(player != null) {
            @Nullable final Integer perPlayer = INITIAL_SPAWN_PER_PLAYER.apply(player.getGameProfile());
            if(perPlayer != null && dimension == perPlayer) return true;
        }

        // Global respawn dimensions override.
        if(new Integer(dimension).equals(INITIAL_SPAWN_DIMENSION)) return true;
        else if(RESPAWN_DIMENSIONS.containsKey(dimension)) return RESPAWN_DIMENSIONS.get(dimension);

        // Global respawn dimensions override (config).
        @Nonnull final WorldRespawnData configData = WorldRespawnData.load();
        if(configData.initial == dimension) return true;
        else if(configData.respawn.containsKey(dimension)) return configData.respawn.get(dimension);

        // Default "respawn-ability".
        return checkBuiltinRespawn && provider.canRespawnHere();
    }

    public static int getInitialSpawnDimension(@Nullable final GameProfile profile) {
        // Per-player initial spawn dimension override.
        if(profile != null) {
            @Nullable final Integer perPlayer = INITIAL_SPAWN_PER_PLAYER.apply(profile);
            if(perPlayer != null) return perPlayer;
        }

        // Global initial spawn dimension override.
        if(INITIAL_SPAWN_DIMENSION != null) return INITIAL_SPAWN_DIMENSION;
        else return WorldRespawnData.load().initial;
    }

    public static boolean isInitialSpawnDimension(@Nonnull final WorldProvider provider) {
        return canSpawnInDimension(provider, null, false) || INITIAL_SPAWN_PER_PLAYER_POSSIBILITIES.contains(provider.getDimension());
    }

    public static final class WorldRespawnData extends WorldSavedData
    {
        @Nonnull
        public final Int2BooleanMap respawn = new Int2BooleanOpenHashMap();
        public int initial;

        @Nonnull
        public static final String ID = NetherAPI.MODID + ":respawn_data";
        public WorldRespawnData(@Nonnull final String id) { super(id); }

        @Override
        public void readFromNBT(@Nonnull final NBTTagCompound compound) {
            initial = compound.getInteger("Initial");
            respawn.clear();

            @Nonnull final NBTTagCompound respawnTag = compound.getCompoundTag("Overrides");
            respawnTag.getKeySet().forEach(dim -> respawn.put(Integer.parseInt(dim), respawnTag.getBoolean(dim)));
        }

        @Nonnull
        @Override
        public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
            @Nonnull final NBTTagCompound respawnTag = new NBTTagCompound();
            respawn.forEach((dim, override) -> respawnTag.setBoolean(dim.toString(), override));

            compound.setInteger("Initial", initial);
            compound.setTag("Overrides", respawnTag);
            return compound;
        }

        @Nonnull
        public static WorldRespawnData load() {
            @Nullable final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if(server == null) return new WorldRespawnData(ID);

            @Nonnull final World overworld = server.getWorld(0);
            @Nullable WorldRespawnData data = (WorldRespawnData)overworld.loadData(WorldRespawnData.class, ID);

            if(data == null) {
                overworld.setData(ID, data = new WorldRespawnData(ID));
                data.initial = NetherAPI.isPerfectSpawnLoaded ? perfectSpawnDim() : NetherAPIConfig.initialSpawnDim;
                data.respawn.putAll(Arrays.stream(NetherAPIConfig.respawnDims)
                        .map(config -> {
                            try {
                                @Nonnull final JsonObject json = JsonUtils.getJsonObject(new JsonParser().parse(config), config);
                                return Pair.of(JsonUtils.getInt(json, "DimId"), JsonUtils.getBoolean(json, "Respawn"));
                            }

                            // Handle malformed entries by outputting error info to the logger (instead of crashing).
                            catch(@Nonnull final JsonParseException e) {
                                NetherAPI.LOGGER.error("An error occurred while parsing \"{}\", skipping...", config, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));

                data.setDirty(true);
            }

            return data;
        }

        // Compatibility with the Perfect Spawn mod.
        private static int perfectSpawnDim() { return AsmHandler.overrideInitialDimension(NetherAPIConfig.initialSpawnDim); }
    }
}
