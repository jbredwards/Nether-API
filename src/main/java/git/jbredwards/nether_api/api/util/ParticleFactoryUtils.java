package git.jbredwards.nether_api.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;

/**
 * Provides some utility functions relating to {@link IParticleFactory}.
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public final class ParticleFactoryUtils
{
    /**
     * @return The {@code IParticleFactory} instance paired to the provided particle type.
     * @throws NullPointerException If particleType is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    @SideOnly(Side.CLIENT)
    public static IParticleFactory getParticleFactory(@Nonnull final EnumParticleTypes particleType) {
        @Nullable final ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
        if(particleManager == null) throw new IllegalStateException("Particle manager not loaded. Make sure this is being called after fml pre init.");
        else return particleManager.particleTypes.get(particleType.getParticleID());
    }

    /**
     * @return A {@code IParticleFactory} instance that uses to provided particle type id.
     * @throws NullPointerException If particleType is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    @SideOnly(Side.CLIENT)
    public static IParticleFactory getParticleFactorySafe(@Nonnull final EnumParticleTypes particleType) {
        @Nullable final IParticleFactory factory = ParticleFactoryUtils.getParticleFactory(particleType);
        return factory == null ? null : (id, world, x, y, z, xSpeed, ySpeed, zSpeed, args) ->
                factory.createParticle(particleType.getParticleID(), world, x, y, z, xSpeed, ySpeed, zSpeed, args);
    }

    /**
     * @return A new {@code IParticleFactory} that has a chance of creating the particle.
     * @throws NullPointerException If particleType or chance are null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nullable
    @SideOnly(Side.CLIENT)
    public static IParticleFactory particleFactoryWithChance(@Nonnull final EnumParticleTypes particleType, @Nonnull final DoubleSupplier chance, @Nullable final int[] params) {
        @Nullable final IParticleFactory factory = ParticleFactoryUtils.getParticleFactory(particleType);
        return factory == null ? null : (id, world, x, y, z, xSpeed, ySpeed, zSpeed, args) -> world.rand.nextFloat() < chance.getAsDouble() ?
                factory.createParticle(particleType.getParticleID(), world, x, y, z, xSpeed, ySpeed, zSpeed, params == null ? args : params) : null;
    }
}
