package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.biome.IAmbienceBiome;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Handle biome ambient sounds and particles from this mod's end
 * @author jbred
 *
 */
public final class TransformerWorldClient implements IClassTransformer, Opcodes
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        if("net.minecraft.client.multiplayer.WorldClient".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            //remove playMoodSoundAndCheckLight method
            classNode.methods.removeIf(method -> method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "playMoodSoundAndCheckLight" : "func_147467_a"));

            //transform showBarrierParticles method
            methods:
            for(final MethodNode method : classNode.methods) {
                if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "showBarrierParticles" : "func_184153_a")) {
                    for(final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * showBarrierParticles: (changes are around line 396)
                         * Old code:
                         * public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * //add biome ambient particles
                         * public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos)
                         * {
                         *     ...
                         *     Hooks.spawnBiomeAmbientParticle(this, random, pos, iblockstate);
                         * }
                         */
                        if(insn.getOpcode() == RETURN) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 5));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 7));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 11));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerWorldClient$Hooks", "spawnBiomeAmbientParticle", "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", false));
                            break methods;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @SideOnly(Side.CLIENT)
        public static void spawnBiomeAmbientParticle(@Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
            if(!state.isFullCube()) {
                final Biome biome = world.getBiome(pos);
                if(biome instanceof IAmbienceBiome) {
                    final IParticleFactory[] factories = ((IAmbienceBiome)biome).getAmbientParticles();
                    if(factories.length == 0) return;

                    else if(factories.length != 1) Collections.shuffle(Arrays.asList(factories), rand);
                    for(final IParticleFactory factory : factories) {
                        final Particle particle = factory.createParticle(-1, world, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0, 0, 0);
                        if(particle != null) {
                            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
                            return;
                        }
                    }
                }
            }
        }
    }
}
