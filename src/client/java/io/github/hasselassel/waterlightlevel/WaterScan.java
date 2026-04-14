package io.github.hasselassel.waterlightlevel;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class WaterScan {
    record PosXZ(int x, int z) {}

    private static ChunkRing2D CHUNKS;

    private static final LongOpenHashSet DIRTY_CHUNKS = new LongOpenHashSet(128);

    private static byte tick = 0;
    private static final byte skipTicks = 10;

    private static PosXZ lastScanPosChunk = null;

    /*private static void fullScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();

        WATER.clear();

        for (int x = px - Config.DISTANCE; x <= px + Config.DISTANCE; x++) {
            for (int y = py - Config.DISTANCE; y <= py + Config.DISTANCE; y++) {
                for (int z = pz - Config.DISTANCE; z <= pz + Config.DISTANCE; z++) {
                    tempPos.set(x, y, z);
                    if (world.getFluidState(tempPos).isIn(FluidTags.WATER)
                            && world.getLightLevel(LightType.BLOCK, tempPos) <= Config.LIGHT_LEVEL) {
                        WATER.add(tempPos.asLong());
                    }
                }
            }
        }

        DIRECTIONS.clear();
        for (long block : WATER) {
            tempPos.set(block);
            int x = tempPos.getX(), y = tempPos.getY(), z = tempPos.getZ();
            byte mask = 0;
            if (!WATER.contains(BlockPos.asLong(x + 1, y, z))) mask |= 1;
            if (!WATER.contains(BlockPos.asLong(x - 1, y, z))) mask |= 1 << 1;
            if (!WATER.contains(BlockPos.asLong(x, y + 1, z))) mask |= 1 << 2;
            if (!WATER.contains(BlockPos.asLong(x, y - 1, z))) mask |= 1 << 3;
            if (!WATER.contains(BlockPos.asLong(x, y, z + 1))) mask |= 1 << 4;
            if (!WATER.contains(BlockPos.asLong(x, y, z - 1))) mask |= 1 << 5;

            if (mask != 0) DIRECTIONS.put(block, mask);
        }

    }*/

    private static void fullScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int px = player.getBlockX();
        int pz = player.getBlockZ();

        int chunkX = ChunkSectionPos.getSectionCoord(px);
        int chunkZ = ChunkSectionPos.getSectionCoord(pz);

        for (int x = -Config.CHUNK_DISTANCE; x <= Config.CHUNK_DISTANCE; x++) {
            for (int z = -Config.CHUNK_DISTANCE; z <= Config.CHUNK_DISTANCE; z++) {
                Chunk c = CHUNKS.getLogical(x, z);
                c.changeChunk(chunkX + x, chunkZ + z);
                c.fullScan(player, world);
            }
        }
    }

    private static void deltaScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        for (Chunk chunk : CHUNKS) {
            if (DIRTY_CHUNKS.contains(chunk.asLong())) {
                chunk.fullScan(player, world);
            } else {
                chunk.deltaScan(player, world);
            }
        }
    }

    protected static void scan(@NotNull MinecraftClient client) {
        var world = client.world;
        var player = client.player;
        if (world == null || player == null) return;

        if (lastScanPosChunk == null) {
            fullScan(player, world);
            var pos = player.getBlockPos();
            int chunkX = ChunkSectionPos.getSectionCoord(pos.getX());
            int chunkZ = ChunkSectionPos.getSectionCoord(pos.getZ());
            lastScanPosChunk = new PosXZ(chunkX, chunkZ);
        } else {
            fixChunkRotationOnPlayerMovement(player, world);
            deltaScan(player, world);
        }
        DIRTY_CHUNKS.clear();
    }

    private static void fixChunkRotationOnPlayerMovement(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int px = player.getBlockX();
        int pz = player.getBlockZ();

        int chunkX = ChunkSectionPos.getSectionCoord(px);
        int chunkZ = ChunkSectionPos.getSectionCoord(pz);

        int dx = chunkX - lastScanPosChunk.x;
        int dz = chunkZ - lastScanPosChunk.z;

        if (dx == 0 && dz == 0) return;

        ChunkIntIntConsumer update = (Chunk c, int x, int z) -> {
            c.changeChunk(lastScanPosChunk.x + x, lastScanPosChunk.z + z);
            c.fullScan(player, world);
        };

        CHUNKS.shift(dx, dz, update);
    }

    protected static void init() {
        CHUNKS = new ChunkRing2D(Config.CHUNK_DISTANCE * 2 + 1, Config.CHUNK_DISTANCE * 2 + 1);

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            lastScanPosChunk = null;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (UI.TOGGLE_AURA_KEY.wasPressed()) UI.toggle_on_off();

            if (!Config.SCAN_ON) return;

            if (tick++ < skipTicks) return;
            tick = 0;

            scan(client);
        });
    }

    public static void markDirty(BlockPos pos) {
        if (lastScanPosChunk == null) return;
        int chunkX = ChunkSectionPos.getSectionCoord(pos.getX());
        int chunkZ = ChunkSectionPos.getSectionCoord(pos.getZ());
        if (Math.abs(lastScanPosChunk.x - chunkX) > Config.CHUNK_DISTANCE
                || Math.abs(lastScanPosChunk.z - chunkZ) > Config.CHUNK_DISTANCE) return;
        long chunkL = Chunk.packInts(chunkX, chunkZ);
        CHUNKS.getLogical(lastScanPosChunk.x - chunkX, lastScanPosChunk.z - chunkZ)
                .markDirty(chunkL);
    }

    public static void markDirty(int chunkX, int chunkZ) {
        if (lastScanPosChunk == null) return;
        if (Math.abs(lastScanPosChunk.x - chunkX) > Config.CHUNK_DISTANCE
                || Math.abs(lastScanPosChunk.z - chunkZ) > Config.CHUNK_DISTANCE) return;
        DIRTY_CHUNKS.add(Chunk.packInts(chunkX, chunkZ));
    }
}

class ChunkRing2D implements Iterable<Chunk> {
    private final Chunk[][] data;
    private final int sizeX;
    private final int sizeZ;
    private int centerX = 0;
    private int centerZ = 0;

    public ChunkRing2D(int sizeX, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.data = new Chunk[sizeX][sizeZ];
    }

    private static int wrap(int value, int size) {
        return Math.floorMod(value, size);
    }

    public Chunk getLogical(int x, int z) {
        return data[wrap(x + centerX, sizeX)][wrap(z + centerZ, sizeZ)];
    }

    public void shift(int dx, int dz) {
        centerX = wrap(centerX + dx, sizeX);
        centerZ = wrap(centerZ + dz, sizeZ);
    }

    public void shift(int dx, int dz, ChunkIntIntConsumer update) {
        this.shift(dx, dz);

        int xUp = sizeX / 2;
        int zUp = sizeZ / 2;

        int xLow = xUp - sizeX + 1;
        int zLow = zUp - sizeZ + 1;

        int absDx = Math.min(Math.abs(dx), sizeX);
        int absDz = Math.min(Math.abs(dz), sizeZ);

        if (dx < 0) {
            for (int x = xLow; x < xLow + absDx; x++) {
                for (int z = zLow; z <= zUp; z++) {
                    update.accept(getLogical(x, z), x, z);
                }
            }
        } else if (dx > 0) {
            for (int x = xUp; x > xUp - absDx; x--) {
                for (int z = zLow; z <= zUp; z++) {
                    update.accept(getLogical(x, z), x, z);
                }
            }
        }

        int zXMin = xLow + (dx < 0 ? absDx : 0);
        int zXMax = xUp - (dx > 0 ? absDx : 0);

        if (dz < 0) {
            for (int z = zLow; z < zLow + absDz; z++) {
                for (int x = zXMin; x <= zXMax; x++) {
                    update.accept(getLogical(x, z), x, z);
                }
            }
        } else if (dz > 0) {
            for (int z = zUp; z > zUp - absDz; z--) {
                for (int x = zXMin; x <= zXMax; x++) {
                    update.accept(getLogical(x, z), x, z);
                }
            }
        }
    }

    @Override
    public @NotNull Iterator<Chunk> iterator() {
        return new Iterator<>() {
            private int x = 0;
            private int z = 0;

            @Override
            public boolean hasNext() {
                return x < sizeX && z < sizeZ;
            }

            @Override
            public Chunk next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Chunk result = data[x][z];
                z++;
                if (z == sizeZ) {
                    z = 0;
                    x++;
                }
                return result;
            }
        };
    }
}

@FunctionalInterface
interface ChunkIntIntConsumer {
    void accept(Chunk a, int b, int c);
}