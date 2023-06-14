package git.jbredwards.nether_api.api.event;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class NetherInitBiomeGensEvent extends WorldTypeEvent
{
    @Nonnull
    public GenLayer[] biomeGenerators;
    public final long seed;

    public NetherInitBiomeGensEvent(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
        super(worldTypeIn);
        seed = seedIn;
        biomeGenerators = originalGeneratorsIn;
    }
}
