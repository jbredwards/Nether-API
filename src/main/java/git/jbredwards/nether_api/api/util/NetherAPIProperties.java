package git.jbredwards.nether_api.api.util;

import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.TransformerInputHandler;
import git.jbredwards.nether_api.mod.common.world.WorldProviderNether;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd;
import git.jbredwards.nether_api.mod.common.world.WorldProviderTheEnd.ExitPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Exposes internal Nether API properties in a stable, non-changing way.
 *
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public final class NetherAPIProperties
{
    /**
     * World generator responsible for creating the End Exit Portal.<br>
     * Should be finalized before or during FML post-init.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static final Mutable<ExitPortal> END_EXIT_PORTAL_GENERATOR = NetherAPIProperties.mutable(
            () -> WorldProviderTheEnd.EXIT_PORTAL, value -> WorldProviderTheEnd.EXIT_PORTAL = value);
    
    /**
     * World generator responsible for creating End Gateways.<br>
     * Should be finalized before or during FML post-init.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static final Mutable<WorldGenerator> END_GATEWAY_GENERATOR = NetherAPIProperties.mutable(
            () -> WorldProviderTheEnd.END_GATEWAY, value -> WorldProviderTheEnd.END_GATEWAY = value);

    /**
     * World generator responsible for creating End Obsidian Pillars.<br>
     * Should be finalized before or during FML post-init.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static final Mutable<WorldGenSpikes> END_OBSIDIAN_PILLAR_GENERATOR = NetherAPIProperties.mutable(
            () -> WorldProviderTheEnd.END_PILLAR, value -> WorldProviderTheEnd.END_PILLAR = value);

    /**
     * World generator responsible for creating the End Spawn Platform.<br>
     * Should be finalized before or during FML post-init.
     */
    @ApiStatus.AvailableSince("1.5.0")
    @Nonnull
    public static final Mutable<WorldGenerator> END_SPAWN_PLATFORM_GENERATOR = NetherAPIProperties.mutable(
            () -> WorldProviderTheEnd.OBSIDIAN_PLATFORM, value -> WorldProviderTheEnd.OBSIDIAN_PLATFORM = value);

    /**
     * Tells NetherEx that the provided block states can be turned into NetherEx's netherrack path.
     * @throws NullPointerException If states is null or has any null entries.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void registerNetherExPathable(@Nonnull final IBlockState... states) {
        for(@Nonnull final IBlockState state : states) {
            TransformerInputHandler.Hooks.NETHERRACK_CANDIDATES.add(Objects.requireNonNull(state));
        }
    }

    /**
     * Sets the result when the game attempts to ignite the End Exit Portal.<br>
     * <br>
     * {@link EnumActionResult#PASS}: Default behavior. Portal ignites if default spawn dimension isn't the end.<br>
     * {@link EnumActionResult#SUCCESS}: Portal always ignites.<br>
     * {@link EnumActionResult#FAIL}: Portal never ignites.
     *
     * @throws NullPointerException If result is null.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void setEndExitPortalIgnitionResult(@Nonnull final EnumActionResult result) {
        WorldProviderTheEnd.initialPortalLit = Objects.requireNonNull(result);
    }

    /**
     * Forces {@link net.minecraft.world.WorldProvider#doesXZShowFog} to return true in the end.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void setForceExtraEndFog(final boolean forceExtraEndFog) {
        WorldProviderTheEnd.forceExtraEndFog = forceExtraEndFog;
    }

    /**
     * Forces {@link net.minecraft.world.WorldProvider#doesXZShowFog} to return true in the nether.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void setForceExtraNetherFog(final boolean forceExtraNetherFog) {
        WorldProviderNether.FORCE_NETHER_FOG = forceExtraNetherFog;
    }

    /**
     * A function to help create generic {@code Mutable} instances.
     */
    @ApiStatus.Internal
    @Nonnull
    private static <T> Mutable<T> mutable(@Nonnull final Supplier<T> getter, @Nonnull final Consumer<T> setter) {
        return new Mutable<T>() {
            @Nonnull
            @Override
            public T getValue() {
                return getter.get();
            }

            @Override
            public void setValue(@Nonnull final T value) {
                setter.accept(Objects.requireNonNull(value));
            }
        };
    }
}
