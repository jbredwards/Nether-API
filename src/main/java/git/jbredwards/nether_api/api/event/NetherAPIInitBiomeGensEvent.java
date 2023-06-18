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
public abstract class NetherAPIInitBiomeGensEvent extends WorldTypeEvent
{
    @Nonnull
    public GenLayer[] biomeGenerators;
    public final long seed;

    public NetherAPIInitBiomeGensEvent(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
        super(worldTypeIn);
        seed = seedIn;
        biomeGenerators = originalGeneratorsIn;
    }

    public static class End extends NetherAPIInitBiomeGensEvent
    {
        public End(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
            super(worldTypeIn, seedIn, originalGeneratorsIn);
        }
    }

    public static class Nether extends NetherAPIInitBiomeGensEvent
    {
        public Nether(@Nonnull WorldType worldTypeIn, long seedIn, @Nonnull GenLayer[] originalGeneratorsIn) {
            super(worldTypeIn, seedIn, originalGeneratorsIn);
        }
    }
}
