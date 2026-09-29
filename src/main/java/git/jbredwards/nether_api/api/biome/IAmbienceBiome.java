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

package git.jbredwards.nether_api.api.biome;

import git.jbredwards.nether_api.api.audio.IDarkSoundAmbience;
import git.jbredwards.nether_api.api.audio.ISoundAmbience;
import git.jbredwards.nether_api.api.audio.impl.DarkSoundAmbience;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Biomes that implement this have access to many new tools for improving the passive biome ambience.
 * This interface is not exclusive to dimensional biomes!
 *
 * @see IAmbienceBiome.Impl
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.0.0")
public interface IAmbienceBiome
{
    /**
     * Air in this biome spawns these once per random block render tick (same as torch particles).
     * One gets picked from random to be played. If the factory returns null, a different factory is randomly chosen.
     * @return The possible ambient particle factories
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nullable
    @SideOnly(Side.CLIENT)
    default IParticleFactory[] getAmbientParticles() {
        return null;
    }

    /**
     * @return The ambient sound that continuously plays while in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nullable
    default SoundEvent getAmbientSound() {
        return null;
    }

    /**
     * @return The ambient sound that randomly plays while in this biome.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nullable
    default ISoundAmbience getRandomAmbientSound() {
        return null;
    }

    /**
     * @return The ambient sound that randomly plays in dark areas (cave sounds).
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.0.0")
    @Nullable
    default IDarkSoundAmbience getDarkAmbienceSound() {
        return DarkSoundAmbience.DEFAULT_CAVE;
    }

    /**
     * @return A new {@code IAmbienceBiome.Delegate} instance.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    static IAmbienceBiome.Delegate delegate() {
        return new Delegate();
    }

    /**
     * The base for {@code IAmbienceBiome} implementations that have mutable properties.
     * <b>All methods added by this interface are intended to only be called by the Biome's owning mod!</b>
     * Otherwise, use {@link git.jbredwards.nether_api.api.event.BiomeAmbienceEvent BiomeAmbienceEvent}.
     * @see IAmbienceBiome.Impl
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    interface Mutable extends IAmbienceBiome
    {
        /**
         * Setter for {@link IAmbienceBiome#getAmbientParticles()}.<br>
         * All null factories are skipped. If the array is empty, null is set.
         * @return Itself.
         * @author jbred.
         */
        @ApiStatus.AvailableSince("1.5.0")
        @Nonnull
        @SideOnly(Side.CLIENT)
        IAmbienceBiome.Mutable setAmbientParticles(@Nullable final IParticleFactory... ambientParticles);

        /**
         * Setter for {@link IAmbienceBiome#getAmbientSound()}.
         * @return Itself.
         * @author jbred.
         */
        @ApiStatus.AvailableSince("1.5.0")
        @Nonnull
        IAmbienceBiome.Mutable setAmbientSound(@Nullable final SoundEvent ambientSound);

        /**
         * Setter for {@link IAmbienceBiome#getRandomAmbientSound()}.
         * @return Itself.
         * @author jbred.
         */
        @ApiStatus.AvailableSince("1.5.0")
        @Nonnull
        IAmbienceBiome.Mutable setRandomAmbientSound(@Nullable final ISoundAmbience randomAmbientSound);

        /**
         * Setter for {@link IAmbienceBiome#getDarkAmbienceSound()}.
         * @return Itself.
         * @author jbred.
         */
        @ApiStatus.AvailableSince("1.5.0")
        @Nonnull
        IAmbienceBiome.Mutable setDarkAmbienceSound(@Nullable final IDarkSoundAmbience darkAmbienceSound);

        /**
         * All null factories are skipped. If the array is empty, null is set.
         * @param ambientParticles The particle factories to add.
         * @return Itself.
         * @author jbred.
         */
        @ApiStatus.AvailableSince("1.5.0")
        @Nonnull
        @SideOnly(Side.CLIENT)
        default IAmbienceBiome.Mutable addAmbientParticles(@Nullable final IParticleFactory... ambientParticles) {
            if(ambientParticles != null) {
                @Nonnull final IParticleFactory[] existing = ArrayUtils.nullToEmpty(this.getAmbientParticles(), IParticleFactory[].class);
                @Nonnull final IParticleFactory[] particles = ArrayUtils.removeAllOccurences(ambientParticles, null);
                if(particles.length != 0) this.setAmbientParticles(existing.length == 0 ? particles : ArrayUtils.addAll(existing, particles));
                else this.setAmbientParticles(existing.length == 0 ? null : existing);
            }

            return this;
        }
    }

    /**
     * An interface that makes it easier for Biomes to implement {@link IAmbienceBiome.Mutable}.
     * Rather than needing to create each getter/setter method yourself, you can use a delegate instance.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    interface Impl extends IAmbienceBiome.Mutable
    {
        /**
         * Must return an <b>existing</b> delegate instance. The usual implementation is as follows:<br>
         * <blockquote><pre>
         * // Holds the ambience properties for this biome.
         * private final IAmbienceBiome.Mutable ambienceBiomeDelegate = IAmbienceBiome.delegate();
         *
         * // Lets IAmbienceBiome.Impl use the delegate ambience properties.
         * public IAmbienceBiome.Mutable getAmbienceBiomeDelegate()
         * {
         *     return this.ambienceBiomeDelegate;
         * }
         * </pre></blockquote>
         *
         * @author jbred
         */
        @ApiStatus.AvailableSince("1.5.0")
        @ApiStatus.OverrideOnly
        @Nonnull
        IAmbienceBiome.Mutable getAmbienceBiomeDelegate();

        // ----------------
        // Wrapped methods.
        // ----------------

        @ApiStatus.NonExtendable
        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        default IParticleFactory[] getAmbientParticles() {
            return this.getAmbienceBiomeDelegate().getAmbientParticles();
        }

        @ApiStatus.NonExtendable
        @Nonnull
        @Override
        @SideOnly(Side.CLIENT)
        default IAmbienceBiome.Impl setAmbientParticles(@Nullable final IParticleFactory... ambientParticles) {
            this.getAmbienceBiomeDelegate().setAmbientParticles(ambientParticles);
            return this;
        }

        @ApiStatus.NonExtendable
        @Nullable
        @Override
        default SoundEvent getAmbientSound() {
            return this.getAmbienceBiomeDelegate().getAmbientSound();
        }

        @ApiStatus.NonExtendable
        @Nonnull
        @Override
        default IAmbienceBiome.Impl setAmbientSound(@Nullable final SoundEvent ambientSound) {
            this.getAmbienceBiomeDelegate().setAmbientSound(ambientSound);
            return this;
        }

        @ApiStatus.NonExtendable
        @Nullable
        @Override
        default ISoundAmbience getRandomAmbientSound() {
            return this.getAmbienceBiomeDelegate().getRandomAmbientSound();
        }

        @ApiStatus.NonExtendable
        @Nonnull
        @Override
        default IAmbienceBiome.Impl setRandomAmbientSound(@Nullable final ISoundAmbience randomAmbientSound) {
            this.getAmbienceBiomeDelegate().setRandomAmbientSound(randomAmbientSound);
            return this;
        }

        @ApiStatus.NonExtendable
        @Nullable
        @Override
        default IDarkSoundAmbience getDarkAmbienceSound() {
            return this.getAmbienceBiomeDelegate().getDarkAmbienceSound();
        }

        @ApiStatus.NonExtendable
        @Nonnull
        @Override
        default IAmbienceBiome.Impl setDarkAmbienceSound(@Nullable final IDarkSoundAmbience darkAmbienceSound) {
            this.getAmbienceBiomeDelegate().setDarkAmbienceSound(darkAmbienceSound);
            return this;
        }
    }

    /**
     * Default implementation of {@link IAmbienceBiome.Mutable} that stores its properties as fields.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    class Delegate implements IAmbienceBiome.Mutable
    {
        @SideOnly(Side.CLIENT)
        @ApiStatus.Internal @Nullable private IParticleFactory[] ambientParticles;
        @ApiStatus.Internal @Nullable private SoundEvent ambientSound;
        @ApiStatus.Internal @Nullable private ISoundAmbience randomAmbientSound;
        @ApiStatus.Internal @Nullable private IDarkSoundAmbience darkAmbienceSound;

        @Nullable
        @SideOnly(Side.CLIENT)
        @Override
        public IParticleFactory[] getAmbientParticles() {
            return this.ambientParticles;
        }

        @Nonnull
        @SideOnly(Side.CLIENT)
        @Override
        public IAmbienceBiome.Delegate setAmbientParticles(@Nullable final IParticleFactory... ambientParticles) {
            if(ambientParticles == null) this.ambientParticles = null;
            else {
                @Nonnull final IParticleFactory[] particles = ArrayUtils.removeAllOccurences(ambientParticles, null);
                this.ambientParticles = particles.length == 0 ? null : particles;
            }

            return this;
        }

        @Nullable
        @Override
        public SoundEvent getAmbientSound() {
            return this.ambientSound;
        }

        @Nonnull
        @Override
        public IAmbienceBiome.Delegate setAmbientSound(@Nullable final SoundEvent ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        @Nullable
        @Override
        public ISoundAmbience getRandomAmbientSound() {
            return this.randomAmbientSound;
        }

        @Nonnull
        @Override
        public IAmbienceBiome.Delegate setRandomAmbientSound(@Nullable final ISoundAmbience randomAmbientSound) {
            this.randomAmbientSound = randomAmbientSound;
            return this;
        }

        @Nullable
        @Override
        public IDarkSoundAmbience getDarkAmbienceSound() {
            return this.darkAmbienceSound;
        }

        @Nonnull
        @Override
        public IAmbienceBiome.Delegate setDarkAmbienceSound(@Nullable final IDarkSoundAmbience darkAmbienceSound) {
            this.darkAmbienceSound = darkAmbienceSound;
            return this;
        }
    }
}
