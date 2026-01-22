package io.github.hasselassel.waterlightlevel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;

public class WaterLightLevelClient implements ClientModInitializer {
	public static final LongOpenHashSet WATER = new LongOpenHashSet(4096);
	@Override
	public void onInitializeClient() {
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level == null || client.player == null) return;

			var level = client.level;
			var player = client.player;

			int r = 16;
			int px = player.getBlockX();
			int py = player.getBlockY();
			int pz = player.getBlockZ();

			WATER.clear();
			BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

			for (int x = px - r; x <= px + r; x++) {
				for (int y = py - r; y <= py + r; y++) {
					for (int z = pz - r; z <= pz + r; z++) {
						blockPos.set(x, y, z);
						if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
							WATER.add(blockPos.asLong());
						}
					}
				}
			}
		});
	}
}

