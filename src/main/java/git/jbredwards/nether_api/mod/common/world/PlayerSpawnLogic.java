/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
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
import lumien.perfectspawn.handler.AsmHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;

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
public final class PlayerSpawnLogic
{
    /**
     * Allows mods to define a global initial spawn dimension.
     */
    @Nullable
    public static Integer INITIAL_SPAWN_DIMENSION = null;

    /**
     * Allows mods to define initial spawn dimension logic on a per-player basis.
     */
    @Nonnull
    public static Function<GameProfile, Integer> INITIAL_SPAWN_PER_PLAYER = profile -> null;

    /**
     * Allows mods to override any dimension's "canRespawnHere", aside from the Overworld.
     */
    @Nonnull
    public static final Int2BooleanMap RESPAWN_DIMENSIONS = new Int2BooleanOpenHashMap();

    public static boolean canSpawnInDimension(@Nonnull final WorldProvider provider, @Nullable final EntityPlayer player) {
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
        return provider.canRespawnHere();
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
            @Nonnull final World overworld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
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
                                System.err.println("An error occurred while parsing \"" + config + "\", skipping...");
                                e.printStackTrace();
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
