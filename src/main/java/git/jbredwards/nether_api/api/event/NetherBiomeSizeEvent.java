package git.jbredwards.nether_api.api.event;

import net.minecraft.world.WorldType;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

import javax.annotation.Nonnull;

/**
 * Fired when determining the size of the biomes to generate.<br>
 * <br>
 * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable Cancelable}.
 * <br>
 * This event does not have a {@link HasResult HasResult}.
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#TERRAIN_GEN_BUS MinecraftForge.TERRAIN_GEN_BUS}.
 * <br>
 * <br>
 * @since 1.0.0
 * @author jbred
 *
 */
public class NetherBiomeSizeEvent extends WorldTypeEvent
{
    public int biomeSize;
    public NetherBiomeSizeEvent(@Nonnull WorldType worldTypeIn) { super(worldTypeIn); }
}
