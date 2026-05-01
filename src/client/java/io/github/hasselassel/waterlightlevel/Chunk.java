package io.github.hasselassel.waterlightlevel;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chunk {
    protected final LongOpenHashSet water = new LongOpenHashSet(4096);
    protected final Long2ByteOpenHashMap directions = new Long2ByteOpenHashMap(1024);

    private final LongOpenHashSet dirty_blocks = new LongOpenHashSet();

    protected final AuraRenderer auraRenderer = new AuraRenderer();

    protected int chunkX;
    protected int chunkZ;

    protected int relativeChunkX;
    protected int relativeChunkZ;

    private static final BlockPos.Mutable tempPos = new BlockPos.Mutable();

    public Chunk(int chunkX, int chunkZ, int relativeChunkX, int relativeChunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.relativeChunkX = relativeChunkX;
        this.relativeChunkZ = relativeChunkZ;
    }

    public void changeChunk(int chunkX, int chunkZ, int relativeChunkX, int relativeChunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.relativeChunkX = relativeChunkX;
        this.relativeChunkZ = relativeChunkZ;
    }

    protected void markDirty(long blockPos) {
        dirty_blocks.add(blockPos);
    }

    protected void fullScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        this.water.clear();

        int py = player.getBlockY();

        int minX = this.chunkX * 16;
        int maxX = minX + 16;

        int minZ = this.chunkZ * 16;
        int maxZ = minZ + 16;

        int minY = py - 8 - Config.CHUNK_DISTANCE * 16;
        int maxY = py + 8 + Config.CHUNK_DISTANCE * 16;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    tempPos.set(x, y, z);
                    if (world.getFluidState(tempPos).isIn(FluidTags.WATER)
                            && world.getLightLevel(LightType.BLOCK, tempPos) <= Config.LIGHT_LEVEL) {
                        this.water.add(tempPos.asLong());
                    }
                }
            }
        }

        this.dirty_blocks.clear();
        this.buildDirections();
    }

    protected void deltaScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int py = player.getBlockY();
        int minY = py - 8 - Config.CHUNK_DISTANCE * 16;
        int maxY = py + 8 + Config.CHUNK_DISTANCE * 16;

        if (dirty_blocks.isEmpty()) return;

        for (long block : this.dirty_blocks) {
            tempPos.set(block);
            int by = tempPos.getY();
            if (minY > by || by > maxY) continue;
            if (world.getFluidState(tempPos).isIn(FluidTags.WATER)
                    && world.getLightLevel(LightType.BLOCK, tempPos) <= Config.LIGHT_LEVEL) {
                this.water.add(tempPos.asLong());
            } else {
                this.water.remove(tempPos.asLong());
            }
        }

        this.dirty_blocks.clear();
        this.buildDirections();
    }

    protected void buildDirections() {
        directions.clear();
        for (long block : water) {
            tempPos.set(block);
            int x = tempPos.getX(), y = tempPos.getY(), z = tempPos.getZ();
            byte mask = 0;
            if (!water.contains(BlockPos.asLong(x + 1, y, z))) mask |= 1;
            if (!water.contains(BlockPos.asLong(x - 1, y, z))) mask |= 1 << 1;
            if (!water.contains(BlockPos.asLong(x, y + 1, z))) mask |= 1 << 2;
            if (!water.contains(BlockPos.asLong(x, y - 1, z))) mask |= 1 << 3;
            if (!water.contains(BlockPos.asLong(x, y, z + 1))) mask |= 1 << 4;
            if (!water.contains(BlockPos.asLong(x, y, z - 1))) mask |= 1 << 5;

            if (mask != 0) directions.put(block, mask);
        }

        auraRenderer.markDirty();
    }

    protected long asLong() {
        return packInts(this.chunkX, this.chunkZ);
    }

    protected static long packInts(int a, int b) {
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }

    /*protected static int unpackFirst(long a) {
        return (int) (a >> 32);
    }

    protected static int unpackSecond(long a) {
        return (int) (a & 0xFFFFFFFFL);
    }*/
}