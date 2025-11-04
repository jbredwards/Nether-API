/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.mod.asm.transformers.modded.betternether;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.common.block.BlockBOPGrass;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.asm.transformers.ITransformer;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import paulevs.betternether.blocks.BlocksRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Allow BetterNether plants to recognize modded soil blocks
 * @author jbred
 *
 */
public final class TransformerBetterNetherPlants implements ITransformer
{
    @Nonnull
    public final Map<String, Info> infoLookup = new HashMap<>();
    public TransformerBetterNetherPlants() {
        infoLookup.put("paulevs.betternether.blocks.BlockBlackApple", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockBlackAppleSeed", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockBlackBush", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c", "canStay"));
        infoLookup.put("paulevs.betternether.blocks.BlockEggPlant", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockEyeSeed", new Info("NETHERRACK_PLANT_TYPE", "DOWN").redirect("getSoilEye").targets(DEOBFUSCATED ? "randomTick" : "func_180645_a", DEOBFUSCATED ? "neighborChanged" : "func_189540_a", DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockEyeVine", new Info("NETHERRACK_PLANT_TYPE", "DOWN").redirect("getSoilEye").targets(DEOBFUSCATED ? "neighborChanged" : "func_189540_a"));
        infoLookup.put("paulevs.betternether.blocks.BlockInkBush", new Info("NETHER_PLANT_TYPE", "UP").targets("canStay"));
        infoLookup.put("paulevs.betternether.blocks.BlockInkBushSeed", new Info("NETHER_PLANT_TYPE", "UP").targets("canStay"));
        infoLookup.put("paulevs.betternether.blocks.BlockLucisSpore", new Info("NETHERRACK_PLANT_TYPE").targets("canAttachTo"));
        infoLookup.put("paulevs.betternether.blocks.BlockMold", new Info("NETHERRACK_PLANT_TYPE", "UP").redirect("getSoilMoss").targets("canStay"));
        infoLookup.put("paulevs.betternether.blocks.BlockNetherGrass", new Info("NETHER_PLANT_TYPE", "UP"));
        infoLookup.put("paulevs.betternether.blocks.BlockNetherReed", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockOrangeMushroom", new Info("NETHERRACK_PLANT_TYPE", "UP").redirect("getSoilMoss").targets("canStay"));
        infoLookup.put("paulevs.betternether.blocks.BlockStalagnateSeed", new Info("NETHERRACK_PLANT_TYPE", "DOWN", "UP").targets(DEOBFUSCATED ? "grow" : "func_176474_b", DEOBFUSCATED ? "updateTick" : "func_180650_b", DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c", DEOBFUSCATED ? "onBlockPlacedBy" : "func_180633_a"));
        infoLookup.put("paulevs.betternether.blocks.BlockStalagnateSeedBottom", new Info("NETHERRACK_PLANT_TYPE", "UP", "DOWN").targets(DEOBFUSCATED ? "grow" : "func_176474_b", DEOBFUSCATED ? "updateTick" : "func_180650_b", DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
        infoLookup.put("paulevs.betternether.blocks.BlockWartSeed", new Info("NETHER_PLANT_TYPE", "UP").targets(DEOBFUSCATED ? "canGrow" : "func_176473_a", DEOBFUSCATED ? "canPlaceBlockAt" : "func_176196_c"));
    }

    @Nonnull
    @Override
    public byte[] transform(@Nonnull final String name, @Nonnull final String transformedName, @Nonnull final byte[] basicClass) {
        @Nonnull final Info info = infoLookup.get(transformedName);
        return transform(basicClass, classNode -> {
            if(info.type != null) transformPlantable(classNode, info.type);
            if(!info.targets.isEmpty()) {
                @Nonnull final Object2IntMap<String> indexLookup = new Object2IntArrayMap<>();
                transformMethod(classNode, method -> info.targets.contains(method.name), (method, insn) -> {
                    if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(DEOBFUSCATED ? "getBlockState" : "func_180495_p")) {
                        final int index = indexLookup.compute(method.name, (key, value) -> value == null ? 1 : value + 1);
                        if(index != 1 || !method.name.equals(DEOBFUSCATED ? "grow" : "func_176474_b")) { // Special case for BlockStalagnateSeed.
                            if(info.direction.length != 0) method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/util/EnumFacing", info.direction[index - 1], "Lnet/minecraft/util/EnumFacing;"));
                            else method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 3)); // Special case for BlockLucisSpore.

                            if(!method.name.equals(DEOBFUSCATED ? "canGrow" : "func_176473_a")) method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            else { // Special case for BlockWartSeed.
                                method.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", DEOBFUSCATED ? "NETHER_WART" : "field_150388_bm", "Lnet/minecraft/block/Block;"));
                                method.instructions.insertBefore(insn, new TypeInsnNode(CHECKCAST, "net/minecraftforge/common/IPlantable"));
                            }

                            method.instructions.insertBefore(insn, genHookMethod(info.redirect, "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraftforge/common/IPlantable;)Lnet/minecraft/block/state/IBlockState;"));
                            method.instructions.remove(insn);

                            if(index >= info.direction.length) return info.targets.size() == 1 ? BreakType.METHODS : BreakType.INSTRUCTIONS;
                        }
                    }

                    return BreakType.CONTINUE;
                });
            }
            /*
             * New code:
             * // Special case for BlockLucisSpore: check the correct block position for the given side.
             * @ASMOverwrite
             * public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
             * {
             *     return side.getAxis().isHorizontal() & this.canAttachTo(worldIn, pos.offset(side.getOpposite()), side);
             * }
             */
            if(info.direction.length == 0) transformMethod(classNode, method -> method.name.equals(DEOBFUSCATED ? "canPlaceBlockOnSide" : "func_176198_a"), (method, insn) -> {
                // remove existing body data
                method.instructions.clear();
                if(method.tryCatchBlocks != null) method.tryCatchBlocks.clear();
                if(method.localVariables != null) method.localVariables.clear();
                if(method.visibleLocalVariableAnnotations != null) method.visibleLocalVariableAnnotations.clear();
                if(method.invisibleLocalVariableAnnotations != null) method.invisibleLocalVariableAnnotations.clear();
                // write new body data
                @Nonnull final GeneratorAdapter generator = new GeneratorAdapter(method, method.access, method.name, method.desc);
                generator.visitVarInsn(ALOAD, 3);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/EnumFacing", DEOBFUSCATED ? "getAxis" : "func_176740_k", "()Lnet/minecraft/util/EnumFacing$Axis;", false);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/EnumFacing$Axis", DEOBFUSCATED ? "isHorizontal" : "func_176722_c", "()Z", false);
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ALOAD, 3);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/EnumFacing", DEOBFUSCATED ? "getOpposite" : "func_176734_d", "()Lnet/minecraft/util/EnumFacing;", false);
                generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", DEOBFUSCATED ? "offset" : "func_177972_a", "(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;", false);
                generator.visitVarInsn(ALOAD, 3);
                generator.visitMethodInsn(INVOKEVIRTUAL, classNode.name, "canAttachTo", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", false);
                generator.visitInsn(IAND);
                generator.returnValue();
                return BreakType.METHODS;
            });
        });
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static IBlockState getSoil(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            @Nonnull final IBlockState soil = world.getBlockState(pos);
            if(!soil.getBlock().canSustainPlant(soil, world, pos, direction, plantable)) return soil;

            @Nonnull final EnumPlantType type = plantable.getPlantType(world, pos.offset(direction));
            return (type == EnumPlantType.Nether ? Blocks.SOUL_SAND : Blocks.NETHERRACK).getDefaultState();
        }

        @Nonnull
        public static IBlockState getSoilEye(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            @Nonnull final IBlockState soil = world.getBlockState(pos);
            if(NetherAPI.isBiomesOPlentyLoaded) { if(soil.getBlock() == BOPBlocks.flesh) return Blocks.NETHERRACK.getDefaultState(); }

            return soil.getBlock().canSustainPlant(soil, world, pos, direction, plantable) ? Blocks.NETHERRACK.getDefaultState() : soil;
        }

        @Nonnull
        public static IBlockState getSoilMoss(@Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final EnumFacing direction, @Nonnull final IPlantable plantable) {
            @Nonnull final IBlockState soil = world.getBlockState(pos);
            if(soil.getBlock() == Blocks.MYCELIUM) return BlocksRegister.BLOCK_NETHER_MYCELIUM.getDefaultState();
            else if(NetherAPIConfig.BetterNether.moldOnMyceliumOnly) {
                if(soil.getBlock() != BlocksRegister.BLOCK_NETHER_MYCELIUM && NetherAPI.isBiomesOPlentyLoaded) {
                    if(soil.getBlock() == BOPBlocks.grass && soil.getValue(BlockBOPGrass.VARIANT) == BlockBOPGrass.BOPGrassType.MYCELIAL_NETHERRACK) return BlocksRegister.BLOCK_NETHER_MYCELIUM.getDefaultState();
                }

                return soil;
            }

            return soil.getBlock().canSustainPlant(soil, world, pos, direction, plantable) ? BlocksRegister.BLOCK_NETHER_MYCELIUM.getDefaultState() : soil;
        }
    }

    // private to prevent other classes from adding these...
    private static final class Info
    {
        @Nonnull final Set<String> targets = new HashSet<>();
        @Nonnull final String[] direction;
        @Nullable final String type;
        @Nonnull String redirect = "getSoil";

        Info(@Nullable final String typeIn, @Nonnull final String... directionIn) {
            direction = directionIn;
            type = typeIn;
        }

        @Nonnull
        Info redirect(@Nonnull final String redirectIn) {
            redirect = redirectIn;
            return this;
        }

        @Nonnull
        Info targets(@Nonnull final String... targetsIn) {
            targets.addAll(Arrays.asList(targetsIn));
            return this;
        }
    }
}
