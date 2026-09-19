package git.jbredwards.nether_api.api.block;

import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Having your Block implement this allows it to control whether its block particles will be colored.
 * @author jbred
 *
 */
@ApiStatus.AvailableSince("1.5.0")
public interface IConditionalParticleColor
{
    /**
     * @param state The block state owning the particle.
     * @return True if the particle can use the block state's color multiplier.
     * @throws NullPointerException If state is null.
     */
    @ApiStatus.AvailableSince("1.5.0")
    boolean particleUseBlockColor(@Nonnull final IBlockState state);
}
