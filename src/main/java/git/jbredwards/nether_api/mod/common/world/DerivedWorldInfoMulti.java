/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.common.world;

import git.jbredwards.nether_api.mod.NetherAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows each dimension to have its own global spawn position.
 * @author jbred
 *
 */
@SuppressWarnings("unused") // used via asm
public class DerivedWorldInfoMulti extends DerivedWorldInfo
{
    @Nonnull
    public final World delegate;
    public final int dimension;

    public DerivedWorldInfoMulti(@Nonnull final WorldInfo worldInfoIn, @Nonnull final World delegateIn, final int dimensionIn) {
        super(worldInfoIn);
        delegate = delegateIn;
        dimension = dimensionIn;
    }

    @Nonnull
    @Override
    public NBTTagCompound cloneNBTCompound(@Nullable final NBTTagCompound nbt) {
        @Nonnull final NBTTagCompound compound = super.cloneNBTCompound(nbt);
        if(dimension != 0) {
            @Nonnull final Data data = Data.load(delegate, dimension);
            compound.setBoolean("initialized", data.initialized);
            compound.setInteger("SpawnX", data.x);
            compound.setInteger("SpawnY", data.y);
            compound.setInteger("SpawnZ", data.z);
        }

        return compound;
    }

    @Override
    public boolean isInitialized() {
        if(dimension == 0) return super.isInitialized();
        else return Data.load(delegate, dimension).initialized;
    }

    @Override
    public void setServerInitialized(final boolean initializedIn) {
        if(dimension == 0) super.setServerInitialized(initializedIn);
        else {
            @Nonnull final Data data = Data.load(delegate, dimension);
            data.initialized = initializedIn;
            data.markDirty();
        }
    }

    @Override
    public int getSpawnX() {
        if(dimension == 0) return super.getSpawnX();
        else return Data.load(delegate, dimension).x;
    }

    @Override
    public int getSpawnY() {
        if(dimension == 0) return super.getSpawnY();
        else return Data.load(delegate, dimension).y;
    }

    @Override
    public int getSpawnZ() {
        if(dimension == 0) return super.getSpawnZ();
        else return Data.load(delegate, dimension).z;
    }

    @Override
    public void setSpawn(@Nonnull final BlockPos spawnPoint) {
        if(dimension == 0) super.setSpawn(spawnPoint);
        else {
            @Nonnull final Data data = Data.load(delegate, dimension);
            data.x = spawnPoint.getX();
            data.y = spawnPoint.getY();
            data.z = spawnPoint.getZ();
            data.markDirty();
        }
    }

    protected static final class Data extends WorldSavedData
    {
        public boolean initialized;
        public int x, y, z;

        @Nonnull
        public static final String ID = NetherAPI.MODID + ":spawn_point_data_";
        public Data(@Nonnull final String name) { super(name); }

        @Override
        public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
            initialized = nbt.getBoolean("Initialized");
            x = nbt.getInteger("X");
            y = nbt.getInteger("Y");
            z = nbt.getInteger("Z");
        }

        @Nonnull
        @Override
        public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
            compound.setBoolean("Initialized", initialized);
            compound.setInteger("X", x);
            compound.setInteger("Y", y);
            compound.setInteger("Z", z);
            return compound;
        }

        @Nonnull
        public static Data load(@Nonnull final World delegate, final int dimension) {
            @Nullable Data data = (Data)delegate.loadData(Data.class, ID + dimension);

            if(data != null) return data;
            delegate.setData(ID + dimension, data = new Data(ID + dimension));
            return data;
        }
    }
}
