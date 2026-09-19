package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Save BiomeStructureHandler caches to biome
 * @author jbred
 *
 */
public final class TransformerBiome implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            classNode.interfaces.add("git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerBiome$Accessor");
            /*
             * Constructor:
             * New code:
             * // Initialize BiomeStructureHandler caches.
             * {
             *     this.nether_api$blocks = Maps.newHashMap();
             *     this.nether_api$entities = Maps.newHashMap();
             *     ...
             * }
             */
            transformMethod(classNode, method -> method.name.equals("<init>"), (method, insn) -> {
                method.instructions.insert(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/world/biome/Biome", "nether_api$blocks", "Ljava/util/Map;"));
                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                method.instructions.insert(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/world/biome/Biome", "nether_api$entities", "Ljava/util/Map;"));
                method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                return BreakType.METHODS;
            });
            /*
             * New code:
             * // Save BiomeStructureHandler block id cache to biome.
             * @ASMGenerated
             * public Map<Pair<String, IBlockState>, IBlockState> nether_api$blocks()
             * {
             *     return this.nether_api$blocks;
             * }
             */
            classNode.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "nether_api$blocks", "Ljava/util/Map;", "Ljava/util/Map<Lorg/apache/commons/lang3/tuple/Pair<Ljava/lang/String;Lnet/minecraft/block/state/IBlockState;>;Lnet/minecraft/block/state/IBlockState;>;", null));
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "nether_api$blocks", "()Ljava/util/Map;", "()Ljava/util/Map<Lorg/apache/commons/lang3/tuple/Pair<Ljava/lang/String;Lnet/minecraft/block/state/IBlockState;>;Lnet/minecraft/block/state/IBlockState;>;", null), generator -> {
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/biome/Biome", "nether_api$blocks", "Ljava/util/Map;");
            });
            /*
             * New code:
             * // Save BiomeStructureHandler entity list cache to biome.
             * @ASMGenerated
             * public Map<Pair<String, EnumCreatureType>, List<Biome.SpawnListEntry>> nether_api$entities()
             * {
             *     return this.nether_api$entities;
             * }
             */
            classNode.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "nether_api$entities", "Ljava/util/Map;", "Ljava/util/Map<Lorg/apache/commons/lang3/tuple/Pair<Ljava/lang/String;Lnet/minecraft/entity/EnumCreatureType;>;Ljava/util/List<Lnet/minecraft/world/biome/Biome$SpawnListEntry;>;>;", null));
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "nether_api$entities", "()Ljava/util/Map;", "()Ljava/util/Map<Lorg/apache/commons/lang3/tuple/Pair<Ljava/lang/String;Lnet/minecraft/entity/EnumCreatureType;>;Ljava/util/List<Lnet/minecraft/world/biome/Biome$SpawnListEntry;>;>;", null), generator -> {
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/biome/Biome", "nether_api$entities", "Ljava/util/Map;");
            });
        });
    }

    @ApiStatus.Internal
    public interface Accessor
    {
        @Nonnull
        Map<Pair<String, IBlockState>, IBlockState> nether_api$blocks();

        @Nonnull
        Map<Pair<String, EnumCreatureType>, List<Biome.SpawnListEntry>> nether_api$entities();
    }
}
