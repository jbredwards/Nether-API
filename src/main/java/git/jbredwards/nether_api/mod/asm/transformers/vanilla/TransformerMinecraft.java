package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.client.audio.BiomeMusicHandler;
import net.minecraft.client.audio.MusicTicker;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allow biomes to have custom music
 * @author jbred
 *
 */
public final class TransformerMinecraft implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transformMethod(basicClass, method -> method.name.equals(DEOBFUSCATED ? "getAmbientMusicType" : "func_147109_W"), (method, insn) -> {
            /*
             * getAmbientMusicType: (changes are around line 3242)
             * Old code:
             * MusicTicker.MusicType type = this.world.provider.getMusicType();
             *
             * New code:
             * // Allow biomes to have custom music.
             * MusicTicker.MusicType type = Hooks.getBiomeMusicType(this.world.provider.getMusicType());
             */
            if(insn.getOpcode() == ASTORE && ((VarInsnNode)insn).var == 1) {
                method.instructions.insertBefore(insn, genHookMethod("getBiomeMusicType", "(Lnet/minecraft/client/audio/MusicTicker$MusicType;)Lnet/minecraft/client/audio/MusicTicker$MusicType;"));
                return BreakType.METHODS;
            }

            return BreakType.CONTINUE;
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nullable
        public static MusicTicker.MusicType getBiomeMusicType(@Nullable final MusicTicker.MusicType fallback) {
            return BiomeMusicHandler.get(fallback);
        }
    }
}
