package git.jbredwards.nether_api.mod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("Nether API Plugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public final class ASMHandler implements IFMLLoadingPlugin
{
    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                //Modded
                "git.jbredwards.nether_api.mod.asm.transformers.modded.TransformerNetherExOverride", //Disable NetherEx's nether override
                //Vanilla
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerMapGenCavesHell", //Ensures that nether caves can carve through any biome
                "git.jbredwards.nether_api.mod.asm.transformers.vanilla.TransformerWorldClient" //Handle biome ambient sounds and particles from this mod's end
        };
    }

    // ==============
    // NOT APPLICABLE
    // ==============

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(@Nonnull Map<String, Object> data) {}

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
