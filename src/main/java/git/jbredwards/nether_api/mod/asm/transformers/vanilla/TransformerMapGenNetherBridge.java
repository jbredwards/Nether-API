package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.event.BiomeStructureEvent;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Allow height to be configured and cache starting biome
 * @author jbred
 *
 */
public final class TransformerMapGenNetherBridge implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            /*
             * New code:
             * // Consistent mob spawning logic for structures.
             * // Also allows MapGenNetherBridge overrides to have better control over custom mob spawning.
             * @ASMGenerated
             * public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, World world, BlockPos pos)
             * {
             *     return Hooks.getPossibleCreatures(this, type, world, pos);
             * }
             */
            if(classNode.name.equals("net/minecraft/world/gen/structure/MapGenNetherBridge")) {
                classNode.interfaces.add("git/jbredwards/nether_api/api/structure/ISpawningStructure");
                transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "getPossibleCreatures", "(Lnet/minecraft/entity/EnumCreatureType;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;", "(Lnet/minecraft/entity/EnumCreatureType;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List<Lnet/minecraft/world/biome/Biome$SpawnListEntry;>;", null), generator -> {
                    generator.loadThis();
                    generator.loadArg(0);
                    generator.loadArg(1);
                    generator.loadArg(2);
                    genHookMethod("getPossibleCreatures", "(Lnet/minecraft/world/gen/structure/MapGenNetherBridge;Lnet/minecraft/entity/EnumCreatureType;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;").accept(generator);
                });
                return;
            }
            /*
             * Constructors:
             * New code:
             * // Create biome holder for fortress structure start.
             * {
             *     this.nether_api$cache = Hooks.createCache();
             *     ...
             * }
             */
            @Nonnull final Type cacheType = Type.getType(Cache.class);
            classNode.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "nether_api$cache", cacheType.getDescriptor(), null, null));
            transformMethod(classNode, method -> method.name.equals("<init>"), (method, insn) -> {
                method.instructions.insert(insn, new FieldInsnNode(PUTFIELD, "net/minecraft/world/gen/structure/MapGenNetherBridge$Start", "nether_api$cache", cacheType.getDescriptor()));
                method.instructions.insert(insn, genHookMethod("createCache", Type.getMethodDescriptor(cacheType)));
                method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                return BreakType.INSTRUCTIONS;
            });
            transformMethod(classNode, method -> method.name.equals("<init>") && method.desc.endsWith("II)V"), (method, insn) -> {
                /*
                 * Constructor: (changes are around 120)
                 * Old code:
                 * {
                 *     ...
                 * }
                 *
                 * New code:
                 * // Initialize biome holder for new fortress structure start.
                 * {
                 *     ...
                 *     Hooks.initCache(this.nether_api$cache, this.components, worldIn, chunkX, chunkZ);
                 * }
                 */
                if(insn.getOpcode() == RETURN) {
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/MapGenNetherBridge$Start", "nether_api$cache", cacheType.getDescriptor()));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/StructureStart", DEOBFUSCATED ? "components" : "field_75075_a", "Ljava/util/List;"));
                    method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                    method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 3));
                    method.instructions.insertBefore(insn, new VarInsnNode(ILOAD, 4));
                    method.instructions.insertBefore(insn, genHookMethod("initCache", Type.getMethodDescriptor(Type.VOID_TYPE, cacheType, Type.getType("Ljava/util/List;"), Type.getType("Lnet/minecraft/world/World;"), Type.INT_TYPE, Type.INT_TYPE)));
                    return BreakType.METHODS;
                }
                /*
                 * Constructor: (changes are around 119)
                 * Old code:
                 * this.setRandomHeight(worldIn, random, 48, 70);
                 *
                 * New code:
                 * // Allow fortress height to be configured.
                 * this.setRandomHeight(worldIn, random, Hooks.getFortressHeightMin(48), Hooks.getFortressHeightMax(70));
                 */
                else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 48) method.instructions.insert(insn, genHookMethod("getFortressHeightMin", "(I)I"));
                else if(insn.getOpcode() == BIPUSH && ((IntInsnNode)insn).operand == 70) method.instructions.insert(insn, genHookMethod("getFortressHeightMax", "(I)I"));
                return BreakType.CONTINUE;
            });
            /*
             * New code:
             * // Read biome from nbt.
             * @ASMGenerated
             * public void readFromNBT(NBTTagCompound tagCompound)
             * {
             *     Hooks.readCache(tagCompound, this.nether_api$cache, this.components);
             * }
             */
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "readFromNBT" : "func_143017_b", "(Lnet/minecraft/nbt/NBTTagCompound;)V", null, null), generator -> {
                generator.loadArg(0);
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/gen/structure/MapGenNetherBridge$Start", "nether_api$cache", cacheType.getDescriptor());
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/gen/structure/StructureStart", DEOBFUSCATED ? "components" : "field_75075_a", "Ljava/util/List;");
                genHookMethod("readCache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("Lnet/minecraft/nbt/NBTTagCompound;"), cacheType, Type.getType("Ljava/util/List;"))).accept(generator);
            });
            /*
             * New code:
             * // Write biome to nbt.
             * @ASMGenerated
             * public void writeToNBT(NBTTagCompound tagCompound)
             * {
             *     Hooks.writeCache(tagCompound, this.nether_api$cache);
             * }
             */
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "writeToNBT" : "func_143022_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V", null, null), generator -> {
                generator.loadArg(0);
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/gen/structure/MapGenNetherBridge$Start", "nether_api$cache", cacheType.getDescriptor());
                genHookMethod("writeCache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("Lnet/minecraft/nbt/NBTTagCompound;"), cacheType)).accept(generator);
            });
            /*
             * New code:
             * // Ensures that the min and max height are never reversed.
             * @ASMGenerated
             * public void setRandomHeight(World worldIn, Random rand, int p_75070_3_, int p_75070_4_)
             * {
             *     super.setRandomHeight(worldIn, rand, Math.min(p_75070_3_, p_75070_4_), Math.max(p_75070_3_, p_75070_4_));
             * }
             */
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "setRandomHeight" : "func_75070_a", "(Lnet/minecraft/world/World;Ljava/util/Random;II)V", null, null), generator -> {
                generator.loadThis();
                generator.loadArg(0);
                generator.loadArg(1);
                generator.loadArg(2);
                generator.loadArg(3);
                generator.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "min", "(II)I", false);
                generator.loadArg(2);
                generator.loadArg(3);
                generator.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
                generator.visitMethodInsn(INVOKESPECIAL,"net/minecraft/world/gen/structure/StructureStart", DEOBFUSCATED ? "setRandomHeight" : "func_75070_a", "(Lnet/minecraft/world/World;Ljava/util/Random;II)V", false);
            });
            /*
             * New code:
             * // Access to fortress structure start biome holder.
             * @ASMGenerated
             * public nether_api$cache()
             * {
             *     return this.nether_api$cache;
             * }
             */
            classNode.interfaces.add("git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerMapGenNetherBridge$Accessor");
            transformOverwrite(classNode, new MethodNode(ACC_PUBLIC | ACC_FINAL, "nether_api$cache", Type.getMethodDescriptor(cacheType), null, null), generator -> {
                generator.loadThis();
                generator.visitFieldInsn(GETFIELD, "net/minecraft/world/gen/structure/MapGenNetherBridge$Start", "nether_api$cache", cacheType.getDescriptor());
            });
        });
    }

    @ApiStatus.Internal
    public interface Accessor
    {
        @Nonnull
        Cache nether_api$cache();
    }

    @ApiStatus.Internal
    public static final class Cache
    {
        @Nullable
        Biome biome;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static Cache createCache() {
            return new Cache();
        }

        public static int getFortressHeightMin(final int min) {
            return NetherAPIConfig.advanced.fortressMinY == -1 ? min : NetherAPIConfig.advanced.fortressMinY;
        }

        public static int getFortressHeightMax(final int max) {
            return NetherAPIConfig.advanced.fortressMaxY == -1 ? max : NetherAPIConfig.advanced.fortressMaxY;
        }

        public static void initCache(@Nonnull final Cache cache, @Nonnull final List<StructureComponent> components, @Nullable final World world, final int chunkX, final int chunkZ) {
            initComponents(cache, world != null ? world.getBiomeProvider().getBiome(new BlockPos((chunkX << 4) + 16, 0, (chunkZ << 4) + 16)) : null, components);
        }

        // Helper.
        private static void initComponents(@Nonnull final Cache cache, @Nullable final Biome biome, @Nonnull final List<StructureComponent> components) {
            cache.biome = biome;
            for(@Nonnull final StructureComponent component : components) {
                if(component instanceof TransformerStructureNetherBridgePieces.Accessor) {
                    ((TransformerStructureNetherBridgePieces.Accessor)component).nether_api$cache(biome);
                }
            }
        }

        public static void readCache(@Nonnull final NBTTagCompound nbt, @Nonnull final Cache cache, @Nonnull final List<StructureComponent> components) {
            initComponents(cache, nbt.hasKey("nether_api:biome", Constants.NBT.TAG_STRING) ? Biome.REGISTRY.getObject(new ResourceLocation(nbt.getString("nether_api:biome"))) : null, components);
        }

        public static void writeCache(@Nonnull final NBTTagCompound nbt, @Nonnull final Cache cache) {
            if(cache.biome != null) nbt.setString("nether_api:biome", Objects.toString(cache.biome.getRegistryName()));
        }

        @Nonnull
        public static List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull final MapGenNetherBridge instance, @Nonnull final EnumCreatureType type, @Nonnull final World world, @Nonnull final BlockPos pos) {
            instance.initializeStructureData(world);
            final boolean checkGround;

            @Nullable StructureStart start = instance.getStructureAt(pos);
            if(start != null) checkGround = false;
            else {
                checkGround = true;
                for(@Nonnull final StructureStart candidate : instance.structureMap.values()) {
                    if(candidate.isSizeableStructure() && candidate.getBoundingBox().isVecInside(pos)) {
                        start = candidate;
                        break;
                    }
                }
            }

            if(start == null) return Collections.emptyList();
            @Nullable final Biome biome = start instanceof Accessor ? ((Accessor)start).nether_api$cache().biome : null;
            if(checkGround && world.getBlockState(pos.down()) != TransformerStructureNetherBridgePieces.Hooks.get(Blocks.NETHER_BRICK.getDefaultState(), biome)) return Collections.emptyList();

            @Nonnull final Supplier<List<Biome.SpawnListEntry>> defaultSpawnList = type == EnumCreatureType.MONSTER ? instance::getSpawnList : Collections::emptyList;
            return biome != null ? BiomeStructureEvent.prepareSpawnList(biome, instance.getStructureName(), type, defaultSpawnList) : defaultSpawnList.get();
        }
    }
}
