package git.jbredwards.nether_api.mod.common.config.ambience;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerBiome;
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

    public static void load() {
        NetherAPI.LOGGER.info("Reading \"ambience.json\"...");
        final long start = System.currentTimeMillis();
        reset();

        if(PARENT.mkdirs() || !FILE.exists()) {
            try(@Nonnull final Writer writer = Files.newWriter(FILE, StandardCharsets.UTF_8)) { writer.write("{\n\n}"); }
            catch(@Nonnull final IOException e) {
                NetherAPI.LOGGER.error("An error occurred while creating the default \"ambience.json\" config file.", e);
                return;
            }
        }

        @Nonnull final JsonElement file;
        try(@Nonnull final Reader reader = Files.newReader(FILE, StandardCharsets.UTF_8)) { file = new JsonParser().parse(reader); }
        catch(@Nonnull final IOException e) {
            NetherAPI.LOGGER.error("An error occurred while reading the \"ambience.json\" config file.", e);
            return;
        }

        if(file.isJsonObject()) for(@Nonnull final Map.Entry<String, JsonElement> entry : file.getAsJsonObject().entrySet()) {
            @Nullable final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(entry.getKey()));
            if(biome == null) {
                NetherAPI.LOGGER.error("Could not get biome from \"{}\", skipping...", entry.getKey());
                continue;
            }

            @Nonnull final Value value;
            try { value = AmbienceDeserializer.INSTANCE.fromJson(entry.getValue(), Value.class); }
            catch(@Nonnull final Exception e) {
                NetherAPI.LOGGER.error("Could not parse custom biome ambience for \"{}\", skipping...", biome.getRegistryName(), e);
                continue;
            }

            ((TransformerBiome.Accessor)biome).nether_api$ambience(value);
        }

        NetherAPI.LOGGER.info("Reading \"ambience.json\" took {} ms", System.currentTimeMillis() - start);
    }

    private static void reset() {
        for(@Nonnull final Biome biome : Biome.REGISTRY) ((TransformerBiome.Accessor)biome).nether_api$ambience(null);
    }
}
