package git.jbredwards.nether_api.mod.common.config.ambience;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import git.jbredwards.nether_api.api.util.ParticleFactoryUtils;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
final class AmbienceDeserializer
{
    @Nonnull
    public static final Gson INSTANCE = (FMLLaunchHandler.side().isClient() ? clientBuilder() : serverBuilder())
            // Passive sound deserializer.
            .registerTypeAdapter(SoundEvent.class, (JsonDeserializer<SoundEvent>)(json, type, ctx) -> {
                @Nonnull final String id = json.isJsonObject() ? JsonUtils.getString(json.getAsJsonObject(), "id") : JsonUtils.getString(json, "sound");
                @Nullable final SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(id));
                if(sound == null) throw new JsonParseException("Expected id to be a sound, was unknown string '" + id + "'");
                return sound;
            })
            // Random sound deserializer.
            .registerTypeAdapter(ISoundAmbience.class, (JsonDeserializer<ISoundAmbience>)(json, type, ctx) -> {
                @Nonnull final SoundEvent sound = JsonUtils.deserializeClass(json, "sound", ctx, SoundEvent.class);
                if(!json.isJsonObject()) return ISoundAmbience.of(sound);

                final float chance = JsonUtils.getFloat(json.getAsJsonObject(), "chance", (float)ISoundAmbience.DEFAULT_CHANCE_PER_TICK);
                return ISoundAmbience.of(sound, chance);
            })
            // Dark sound deserializer.
            .registerTypeAdapter(IDarkSoundAmbience.class, (JsonDeserializer<IDarkSoundAmbience>)(json, type, ctx) -> {
                @Nonnull final SoundEvent sound = JsonUtils.deserializeClass(json, "sound", ctx, SoundEvent.class);
                if(!json.isJsonObject()) return IDarkSoundAmbience.vanilla(sound);

                final float chance = JsonUtils.getFloat(json.getAsJsonObject(), "chance", (float)IDarkSoundAmbience.DEFAULT_CHANCE_PER_TICK);
                final int lightSearchRadius = JsonUtils.getInt(json.getAsJsonObject(), "light_search_radius", IDarkSoundAmbience.DEFAULT_LIGHT_SEARCH_RADIUS);
                final float soundOffset = JsonUtils.getFloat(json.getAsJsonObject(), "sound_offset", (float)IDarkSoundAmbience.DEFAULT_SOUND_OFFSET);
                return IDarkSoundAmbience.modded(sound, chance, lightSearchRadius, soundOffset);
            })
            .setLenient()
            .create();

    // Client-side deserializers.
    @Nonnull
    private static GsonBuilder clientBuilder() {
        return new GsonBuilder()
                // Base deserializer.
                .registerTypeAdapter(AmbienceConfig.Value.class, (JsonDeserializer<AmbienceConfig.Value>)(json, type, ctx) -> {
                    @Nonnull final AmbienceConfig.Value cfg = new AmbienceConfig.Value();
                    cfg.ambience = JsonUtils.deserializeClass(json.getAsJsonObject(), "ambience", null, ctx, IAmbienceBiome.class);
                    cfg.netherFogColor = Optional.ofNullable(JsonUtils.deserializeClass(json.getAsJsonObject(), "nether_fog_color", null, ctx, Color.class)).map(c -> c.getColorComponents(null)).orElse(null);
                    cfg.endFogColor = Optional.ofNullable(JsonUtils.deserializeClass(json.getAsJsonObject(), "end_fog_color", null, ctx, Color.class)).map(c -> c.getColorComponents(null)).orElse(null);
                    return cfg;
                })
                // Ambience deserializer.
                .registerTypeAdapter(IAmbienceBiome.class, (JsonDeserializer<IAmbienceBiome>)(json, type, ctx) -> {
                    @Nullable final IParticleFactory[] particles = JsonUtils.deserializeClass(json.getAsJsonObject(), "particles", null, ctx, IParticleFactory[].class);
                    @Nullable final SoundEvent passiveSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "passive_sound", null, ctx, SoundEvent.class);
                    @Nullable final ISoundAmbience randomSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "random_sound", null, ctx, ISoundAmbience.class);
                    @Nullable final IDarkSoundAmbience darkSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "dark_sound", null, ctx, IDarkSoundAmbience.class);
                    return IAmbienceBiome.delegate().setAmbientParticles(particles).setAmbientSound(passiveSound).setRandomAmbientSound(randomSound).setDarkAmbienceSound(darkSound);
                })
                // Particle factory deserializer.
                .registerTypeAdapter(IParticleFactory.class, (JsonDeserializer<IParticleFactory>)(json, type, ctx) -> {
                    @Nonnull final String id = json.isJsonObject() ? JsonUtils.getString(json.getAsJsonObject(), "id") : JsonUtils.getString(json, "particle");
                    @Nullable final EnumParticleTypes particle = EnumParticleTypes.getByName(id);

                    if(particle == null) throw new JsonParseException("Expected id to be a particle, was unknown string '" + id + "'");
                    else if(!json.isJsonObject()) return ParticleFactoryUtils.getParticleFactorySafe(particle);

                    final float chance = JsonUtils.getFloat(json.getAsJsonObject(), "chance", 1);
                    return ParticleFactoryUtils.particleFactoryWithChance(particle, () -> chance);
                })
                // Color deserializer.
                .registerTypeAdapter(Color.class, (JsonDeserializer<Color>)(json, type, ctx) -> Color.decode(JsonUtils.getString(json, "color")));
    }

    // Server-side deserializers.
    @Nonnull
    private static GsonBuilder serverBuilder() {
        return new GsonBuilder()
                // Base deserializer.
                .registerTypeAdapter(AmbienceConfig.Value.class, (JsonDeserializer<AmbienceConfig.Value>)(json, type, ctx) -> {
                    @Nonnull final AmbienceConfig.Value cfg = new AmbienceConfig.Value();
                    cfg.ambience = JsonUtils.deserializeClass(json.getAsJsonObject(), "ambience", null, ctx, IAmbienceBiome.class);
                    return cfg;
                })
                // Ambience deserializer.
                .registerTypeAdapter(IAmbienceBiome.class, (JsonDeserializer<IAmbienceBiome>)(json, type, ctx) -> {
                    @Nullable final SoundEvent passiveSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "passive_sound", null, ctx, SoundEvent.class);
                    @Nullable final ISoundAmbience randomSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "random_sound", null, ctx, ISoundAmbience.class);
                    @Nullable final IDarkSoundAmbience darkSound = JsonUtils.deserializeClass(json.getAsJsonObject(), "dark_sound", null, ctx, IDarkSoundAmbience.class);
                    return IAmbienceBiome.delegate().setAmbientSound(passiveSound).setRandomAmbientSound(randomSound).setDarkAmbienceSound(darkSound);
                });
    }
}
