/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.nether_api.mod.common.config.ambience;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBiome;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@ApiStatus.Internal
public final class AmbienceConfig
{
    @Nonnull static final File PARENT = new File(Loader.instance().getConfigDir(), NetherAPI.MODID);
    @Nonnull static final File FILE = new File(PARENT, "ambience.json");

    @Nullable
    static Value get(@Nonnull final Biome biome) { return (Value)((TransformerBiome.Accessor)biome).nether_api$ambience(); }
    static final class Value
    {
        @Nullable public IAmbienceBiome ambience;
        @Nullable public float[] netherFogColor;
        @Nullable public float[] endFogColor;
    }

    @Nullable
    public static JsonElement read() {
        if(PARENT.mkdirs() || !FILE.exists()) {
            try(@Nonnull final Writer writer = Files.newWriter(FILE, StandardCharsets.UTF_8)) { writer.write("{\n\n}"); }
            catch(@Nonnull final IOException e) {
                NetherAPI.LOGGER.error("An error occurred while creating the default \"ambience.json\" config file.", e);
                return null;
            }
        }

        try(@Nonnull final Reader reader = Files.newReader(FILE, StandardCharsets.UTF_8)) { return new JsonParser().parse(reader); }
        catch(@Nonnull final Exception e) {
            NetherAPI.LOGGER.error("An error occurred while reading the \"ambience.json\" config file.", e);
            return null;
        }
    }

    public static boolean load() { return load(read()); }
    public static boolean load(@Nullable final JsonElement file) {
        NetherAPI.LOGGER.info("Reading \"ambience.json\"...");
        final long start = System.currentTimeMillis();
        reset();

        if(file == null) return false;
        else if(!file.isJsonObject()) {
            NetherAPI.LOGGER.error("Expected \"ambience.json\" to be a JsonObject, was {}", JsonUtils.toString(file));
            return false;
        }

        boolean foundError = false;
        for(@Nonnull final Map.Entry<String, JsonElement> entry : file.getAsJsonObject().entrySet()) {
            @Nullable final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(entry.getKey()));
            if(biome == null) {
                NetherAPI.LOGGER.error("Could not get biome from \"{}\", skipping...", entry.getKey());
                foundError = true;
                continue;
            }

            @Nonnull final Value value;
            try { value = AmbienceDeserializer.INSTANCE.fromJson(entry.getValue(), Value.class); }
            catch(@Nonnull final Exception e) {
                NetherAPI.LOGGER.error("Could not parse custom biome ambience for \"{}\", skipping...", biome.getRegistryName(), e);
                foundError = true;
                continue;
            }

            ((TransformerBiome.Accessor)biome).nether_api$ambience(value);
        }

        NetherAPI.LOGGER.info("Reading \"ambience.json\" took {} ms", System.currentTimeMillis() - start);
        return !foundError;
    }

    private static void reset() {
        for(@Nonnull final Biome biome : Biome.REGISTRY) ((TransformerBiome.Accessor)biome).nether_api$ambience(null);
    }
}
