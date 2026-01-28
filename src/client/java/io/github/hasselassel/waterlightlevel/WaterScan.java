package io.github.hasselassel.waterlightlevel;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

class WaterScan {
    private static BlockPos.Mutable playerLastPos = new BlockPos.Mutable();
    private static final LongOpenHashSet WATER = new LongOpenHashSet(4096);

    protected static final Long2ByteOpenHashMap DIRECTIONS = new Long2ByteOpenHashMap(4096);

    private static byte tick = 0, skipTicks = 10;
    protected static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (UI.TOGGLE_AURA_KEY.wasPressed()) Config.TURNED_ON = !Config.TURNED_ON;
            if (!Config.TURNED_ON) return;

            if (tick++ >= skipTicks) {
                tick = 0;
                return;
            }

            var world = client.world;
            var player = client.player;
            if (client.world == null || client.player == null) return;

            var playerBlockPos = player.getBlockPos();
            if (playerLastPos.equals(playerBlockPos)) return;

            int px = player.getBlockX();
            int py = player.getBlockY();
            int pz = player.getBlockZ();

            WATER.clear();
            BlockPos.Mutable blockPos = new BlockPos.Mutable();

            for (int x = px - Config.DISTANCE; x <= px + Config.DISTANCE; x++) {
                for (int y = py - Config.DISTANCE; y <= py + Config.DISTANCE; y++) {
                    for (int z = pz - Config.DISTANCE; z <= pz + Config.DISTANCE; z++) {
                        blockPos.set(x, y, z);
                        if (world.getFluidState(blockPos).isIn(FluidTags.WATER)
                                && world.getLightLevel(LightType.BLOCK, blockPos) <= Config.LIGHT_LEVEL) {
                            WATER.add(blockPos.asLong());
                        }
                    }
                }
            }

            DIRECTIONS.clear();
            for (long block : WATER) {
                blockPos.set(block);
                int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
                byte mask = 0;
                if (!WATER.contains(BlockPos.asLong(x+1,y,z))) mask |= 1;
                if (!WATER.contains(BlockPos.asLong(x-1,y,z))) mask |= 1<<1;
                if (!WATER.contains(BlockPos.asLong(x,y+1,z))) mask |= 1<<2;
                if (!WATER.contains(BlockPos.asLong(x,y-1,z))) mask |= 1<<3;
                if (!WATER.contains(BlockPos.asLong(x,y,z+1))) mask |= 1<<4;
                if (!WATER.contains(BlockPos.asLong(x,y,z-1))) mask |= 1<<5;

                if (mask != 0) DIRECTIONS.put(block, mask);
            }
        });
    }
}
