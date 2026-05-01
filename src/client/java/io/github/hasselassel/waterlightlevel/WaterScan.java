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
    static class PosXZ {
        protected int x;
        protected int z;

        public PosXZ(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    protected static ChunkRing2D CHUNKS;

    private static final LongOpenHashSet DIRTY_CHUNKS = new LongOpenHashSet(128);

    private static byte tick = 0;
    private static final byte skipTicks = 10;

    private static PosXZ lastScanPosChunk = null;

    private static void fullScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int px = player.getBlockX();
        int pz = player.getBlockZ();

        int chunkX = ChunkSectionPos.getSectionCoord(px);
        int chunkZ = ChunkSectionPos.getSectionCoord(pz);

        for (int x = -Config.CHUNK_DISTANCE; x <= Config.CHUNK_DISTANCE; x++) {
            for (int z = -Config.CHUNK_DISTANCE; z <= Config.CHUNK_DISTANCE; z++) {
                Chunk c = CHUNKS.getLogical(x, z);
                c.changeChunk(chunkX + x, chunkZ + z, x, z);
                c.fullScan(player, world);
            }
        }
    }

    private static void deltaScan(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int fulldelta = 0, deltadelta = 0;
        for (Chunk chunk : CHUNKS) {
            if (DIRTY_CHUNKS.contains(chunk.asLong())) {
                fulldelta++;
                chunk.fullScan(player, world);
            } else {
                deltadelta++;
                chunk.deltaScan(player, world);
            }
        }
        System.out.println("FULL DELTA SCANS: " + fulldelta + ", DELTA DELTA SCANS: " + deltadelta);
    }

    protected static boolean scan(@NotNull MinecraftClient client) {
        var world = client.world;
        var player = client.player;
        if (world == null || player == null) return false;

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
        return true;
    }

    private static void fixChunkRotationOnPlayerMovement(@NotNull ClientPlayerEntity player, @NotNull ClientWorld world) {
        int px = player.getBlockX();
        int pz = player.getBlockZ();

        int chunkX = ChunkSectionPos.getSectionCoord(px);
        int chunkZ = ChunkSectionPos.getSectionCoord(pz);

        int dx = chunkX - lastScanPosChunk.x;
        int dz = chunkZ - lastScanPosChunk.z;

        lastScanPosChunk.x = chunkX;
        lastScanPosChunk.z = chunkZ;

        if (dx == 0 && dz == 0) return;

        ChunkIntIntConsumer update = (Chunk c, int x, int z) -> {
            c.changeChunk(chunkX + x, chunkZ + z);
            c.fullScan(player, world);
        };

        CHUNKS.shift(dx, dz, update);
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

    protected static void init() {
        CHUNKS = new ChunkRing2D(Config.CHUNK_DISTANCE * 2 + 1, Config.CHUNK_DISTANCE * 2 + 1);

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> lastScanPosChunk = null);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (UI.TOGGLE_AURA_KEY.wasPressed()) UI.toggle_on_off();

            if (!Config.SCAN_ON) return;

            if (tick++ < skipTicks) return;
            tick = 0;

            scan(client);
        });
    }
}