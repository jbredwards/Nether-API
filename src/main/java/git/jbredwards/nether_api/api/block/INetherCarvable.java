package git.jbredwards.nether_api.api.block;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface INetherCarvable
{
    void onNetherCarveThrough(@Nonnull IBlockState state);
}
