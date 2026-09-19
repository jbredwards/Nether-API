package git.jbredwards.nether_api.api.util;

import git.jbredwards.nether_api.mod.asm.transformers.modded.netherex.TransformerInputHandler;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Objects;

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
     * Tells NetherEx that the provided block states can be turned into NetherEx's netherrack path.
     * @throws NullPointerException If states is null or has any null entries.
     * @author jbred
     */
    @ApiStatus.AvailableSince("1.5.0")
    public static void registerNetherExPathable(@Nonnull final IBlockState... states) {
        for(@Nonnull final IBlockState state : states) TransformerInputHandler.Hooks.NETHERRACK_CANDIDATES.add(Objects.requireNonNull(state));
    }
}
