package io.github.hasselassel.waterlightlevel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public class AuraRenderer {
	public static void init() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(render_context -> {
            Camera cam;
            cam = render_context.gameRenderer().getMainCamera();
            double cx = cam.position().x;
            double cy = cam.position().y;
            double cz = cam.position().z;

            //weita
        });
    }
}
