package git.jbredwards.nether_api.mod.asm.transformers.vanilla;

import git.jbredwards.nether_api.api.event.BiomeStructureEvent;
import git.jbredwards.nether_api.api.util.NetherGenerationUtils;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Use event to get structure block IDs
 * @author jbred
 *
 */
public final class TransformerStructureNetherBridgePieces implements ITransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        return transform(basicClass, classNode -> {
            if(classNode.name.equals("net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece")) {
                classNode.interfaces.add("git/jbredwards/nether_api/mod/asm/transformers/vanilla/TransformerStructureNetherBridgePieces$Accessor");
                classNode.fields.add(new FieldNode(ACC_PUBLIC, "nether_api$cache", "Lnet/minecraft/world/biome/Biome;", null, null));
                /*
                 * New code:
                 * // Setter for the biome of this structure piece.
                 * @ASMGenerated
                 * public void nether_api$cache(Biome biome)
                 * {
                 *     this.nether_api$cache = biome;
                 * }
                 */
                transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, "nether_api$cache", "(Lnet/minecraft/world/biome/Biome;)V", null, null), generator -> {
                    generator.loadThis();
                    generator.loadArg(0);
                    generator.visitFieldInsn(PUTFIELD, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", "nether_api$cache", "Lnet/minecraft/world/biome/Biome;");
                });
                /*
                 * New code:
                 * // Allow pillar distance to be configured.
                 * @ASMGenerated
                 * public void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
                 * {
                 *     Hooks.replaceAirAndLiquidDownwards(worldIn, blockstateIn, this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z), boundingboxIn);
                 * }
                 */
                transformOverwrite(classNode, new MethodNode(ACC_PUBLIC, DEOBFUSCATED ? "replaceAirAndLiquidDownwards" : "func_175808_b", "(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;IIILnet/minecraft/world/gen/structure/StructureBoundingBox;)V", null, null), generator -> {
                    generator.loadArg(0);
                    generator.loadArg(1);
                    generator.loadThis();
                    generator.loadArg(2);
                    generator.loadArg(4);
                    generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", DEOBFUSCATED ? "getXWithOffset" : "func_74865_a", "(II)I", false);
                    generator.loadThis();
                    generator.loadArg(3);
                    generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", DEOBFUSCATED ? "getYWithOffset" : "func_74862_a", "(I)I", false);
                    generator.loadThis();
                    generator.loadArg(2);
                    generator.loadArg(4);
                    generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", DEOBFUSCATED ? "getZWithOffset" : "func_74873_b", "(II)I", false);
                    generator.loadArg(5);
                    genHookMethod("replaceAirAndLiquidDownwards", "(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;IIILnet/minecraft/world/gen/structure/StructureBoundingBox;)V").accept(generator);
                });
            }

            else transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "addComponentParts" : "func_74875_a"), (method, insn) -> {
                /*
                 * addComponentParts:
                 * Old code:
                 * ... BLOCK_TO_PLACE.getDefaultState() ...
                 *
                 * New code:
                 * // Allow fortress blocks to be biome-dependant.
                 * ... Hooks.get(BLOCK_TO_PLACE.getDefaultState(), this.nether_api$cache) ...
                 */
                if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getDefaultState" : "func_176223_P")) {
                    method.instructions.insert(insn, genHookMethod("get", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/biome/Biome;)Lnet/minecraft/block/state/IBlockState;"));
                    method.instructions.insert(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", "nether_api$cache", "Lnet/minecraft/world/biome/Biome;"));
                    method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                }
                /*
                 * addComponentParts: (changes around line 1509)
                 * Old code:
                 * ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic().setEntityId(EntityList.getKey(EntityBlaze.class));
                 *
                 * New code:
                 * // Allow fortress mob spawners to be biome-dependant.
                 * Hooks.applyEntity(((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic(), EntityList.getKey(EntityBlaze.class), randomIn, this.nether_api$cache);
                 */
                else if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "setEntityId" : "func_190894_a")) {
                    method.instructions.insert(insn, genHookMethod("applyEntity", "(Lnet/minecraft/tileentity/MobSpawnerBaseLogic;Lnet/minecraft/util/ResourceLocation;Ljava/util/Random;Lnet/minecraft/world/biome/Biome;)V"));
                    method.instructions.insert(insn, new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/structure/StructureNetherBridgePieces$Piece", "nether_api$cache", "Lnet/minecraft/world/biome/Biome;"));
                    method.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
                    method.instructions.insert(insn, new VarInsnNode(ALOAD, 2));
                    method.instructions.remove(insn);
                }

                return BreakType.CONTINUE;
            });
        });
    }

    @ApiStatus.Internal
    public interface Accessor
    {
        void nether_api$cache(@Nullable final Biome biome);
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void applyEntity(@Nonnull final MobSpawnerBaseLogic spawnerLogic, @Nonnull final ResourceLocation entity, @Nonnull final Random random, @Nullable final Biome biome) {
            if(biome != null) {
                @Nonnull final BiomeStructureEvent.PrepareSpawnerLogic event = new BiomeStructureEvent.PrepareSpawnerLogic(biome, "Fortress", spawnerLogic, entity, random);
                try { if(MinecraftForge.TERRAIN_GEN_BUS.post(event)) return; }
                catch(@Nonnull final Throwable e) { NetherAPI.LOGGER.error("Error while preparing spawner of type \"{}\" for \"Fortress\"", entity, e); }
            }

            spawnerLogic.setEntityId(entity);
        }

        @Nonnull
        public static IBlockState get(@Nonnull final IBlockState state, @Nullable final Biome biome) {
            return biome != null ? BiomeStructureEvent.getBlockId(biome, "Fortress", state) : state;
        }

        public static void replaceAirAndLiquidDownwards(@Nonnull final World worldIn, @Nonnull final IBlockState state, final int x, final int y, final int z, @Nonnull final StructureBoundingBox box) {
            @Nonnull final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
            if(box.isVecInside(pos)) {
                final int cfg = NetherAPIConfig.advanced.fortressPillarMinLen >= NetherAPIConfig.advanced.fortressPillarMaxLen ? NetherAPIConfig.advanced.fortressPillarMinLen
                        : MathHelper.getInt(NetherGenerationUtils.createSeedRandom(worldIn.getSeed(), x, z), NetherAPIConfig.advanced.fortressPillarMinLen, NetherAPIConfig.advanced.fortressPillarMaxLen);
                for(final int minHeight = cfg == -1 ? 1 : pos.getY() - cfg; pos.getY() > minHeight && (worldIn.isAirBlock(pos) || worldIn.getBlockState(pos).getMaterial().isLiquid()); pos.setPos(x, pos.getY() - 1, z)) {
                    worldIn.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
                }
            }
        }
    }
}
