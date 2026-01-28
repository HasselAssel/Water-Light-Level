package io.github.hasselassel.waterlightlevel;


import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

class AuraRenderer {
	protected static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(render_ctx -> {
            if (!Config.TURNED_ON) return;

            MatrixStack matrices = render_ctx.matrices();
            VertexConsumerProvider vcp = render_ctx.consumers();

            Vec3d camPos = render_ctx.gameRenderer().getCamera().getCameraPos();

            VertexConsumer vc = vcp.getBuffer(RenderLayers.debugFilledBox());

            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            var matrix = matrices.peek();
            var blockMut = new BlockPos.Mutable();
            WaterScan.DIRECTIONS.forEach((block, mask) -> {
                blockMut.set(block);
                float x = blockMut.getX(), y = blockMut.getY(), z = blockMut.getZ();
                drawMaskedBlock(vc, matrix, x, y, z, Config.ARGB, mask);
            });

            matrices.pop();
        });
    }

    private static void v(VertexConsumer vc, MatrixStack.Entry e, float x, float y, float z, int argb) {
        vc.vertex(e, x, y, z).color(argb);
    }

    private static void quad(VertexConsumer vc, MatrixStack.Entry e,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int argb) {
        v(vc, e, x1, y1, z1, argb);
        v(vc, e, x2, y2, z2, argb);
        v(vc, e, x3, y3, z3, argb);
        v(vc, e, x4, y4, z4, argb);
    }

    private static void drawMaskedBlock(VertexConsumer vc, MatrixStack.Entry e,
                                       float x, float y, float z,
                                       int argb, byte mask) {
        // +X
        if ((mask & (1)) != 0)
            quad(vc, e, x+1,y,z+1,  x+1,y,z,  x+1,y+1,z,  x+1,y+1,z+1, argb);
        // -X
        if ((mask & (1 << 1)) != 0)
            quad(vc, e, x,y,z,  x,y,z+1,  x,y+1,z+1,  x,y+1,z, argb);
        // +Y
        if ((mask & (1 << 2)) != 0)
            quad(vc, e, x,y+1,z,  x,y+1,z+1,  x+1,y+1,z+1,  x+1,y+1,z, argb);
        // -Y
        if ((mask & (1 << 3)) != 0)
            quad(vc, e, x,y,z+1,  x,y,z,  x+1,y,z,  x+1,y,z+1, argb);
        // +Z
        if ((mask & (1 << 4)) != 0)
            quad(vc, e, x,y,z+1,  x+1,y,z+1,  x+1,y+1,z+1,  x,y+1,z+1, argb);
        // -Z
        if ((mask & (1 << 5)) != 0)
            quad(vc, e, x+1,y,z,  x,y,z,  x,y+1,z,  x+1,y+1,z, argb);
    }
}
