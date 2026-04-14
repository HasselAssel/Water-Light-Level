package io.github.hasselassel.waterlightlevel.mixin;

import io.github.hasselassel.waterlightlevel.WaterScan;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void waterlightlevel$onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        WaterScan.markDirty(packet.getPos());
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
    private void waterlightlevel$onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
        packet.visitUpdates((BlockPos pos, BlockState state) -> {
            WaterScan.markDirty(pos);
        });
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void waterlightlevel$onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        int chunkX = packet.getChunkX();
        int chunkZ = packet.getChunkZ();
        WaterScan.markDirty(chunkX, chunkZ);
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void waterlightlevel$onLightUpdate(LightUpdateS2CPacket packet, CallbackInfo ci) {
        int chunkX = packet.getChunkX();
        int chunkZ = packet.getChunkZ();
        WaterScan.markDirty(chunkX, chunkZ);
    }
}