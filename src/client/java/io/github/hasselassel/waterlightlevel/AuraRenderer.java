package io.github.hasselassel.waterlightlevel;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class AuraRenderer {
    private MappableRingBuffer mappableRingBuffer;
    private GpuBuffer indexBuffer;
    private BuiltBuffer.DrawParameters drawParameters;

    private static final BufferAllocator allocator = new BufferAllocator(1 << 16);

    private final BlockPos.Mutable tempPos = new BlockPos.Mutable();

    private void rebuild() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR
        );

        for (var entry : WaterScan.DIRECTIONS.long2ByteEntrySet()) {
            tempPos.set(entry.getLongKey());
            byte mask = entry.getByteValue();
            storeMaskedBlock(bufferBuilder, tempPos.getX(), tempPos.getY(), tempPos.getZ(), Config.ARGB, mask);
        }

        indexBuffer = null;
        drawParameters = null;

        try (BuiltBuffer builtBuffer = bufferBuilder.endNullable()) {
            if (builtBuffer == null) return;

            ByteBuffer vertexBytes = builtBuffer.getBuffer();
            int vertexSize = vertexBytes.remaining();

            if (vertexSize == 0) return;

            if (mappableRingBuffer == null || mappableRingBuffer.size() < vertexSize) {
                if (mappableRingBuffer != null) mappableRingBuffer.close();

                mappableRingBuffer = new MappableRingBuffer(
                        () -> "Water Light Level Aura Vertices",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                        vertexSize
                );
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.writeToBuffer(
                    mappableRingBuffer.getBlocking().slice(0, vertexSize),
                    vertexBytes
            );

            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().getVertexSorter());
            ByteBuffer indexByteBuffer = builtBuffer.getSortedBuffer();
            if (indexByteBuffer == null) return;

            indexBuffer = VertexFormats.POSITION_COLOR.uploadImmediateIndexBuffer(indexByteBuffer);
            drawParameters = builtBuffer.getDrawParameters();
        }
    }

    protected static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(render_ctx -> {
            if (!Config.AURA_ON) return;

            if (dirty) rebuild();

            if (indexBuffer == null || drawParameters == null || mappableRingBuffer == null) return;

            var camPos = render_ctx.gameRenderer().getCamera().getCameraPos();
            Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

            Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
            Vector3f MODEL_OFFSET = new Vector3f();
            Matrix4f TEXTURE_MATRIX = new Matrix4f();
            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                    .write(modelView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

            RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Water Light Level Aura Renderer",
                            MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView(),
                            OptionalInt.empty(),
                            MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView(),
                            OptionalDouble.empty()
                    );
            renderPass.setPipeline(RenderLayers.debugFilledBox().getRenderPipeline());
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            var vertexBuffer = mappableRingBuffer.getBlocking();
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, drawParameters.indexType());
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
            renderPass.close();
        });
    }

    private static void v(BufferBuilder bb, float x, float y, float z, int argb) {
        bb.vertex(x, y, z).color(argb);
    }

    private static void quad(BufferBuilder bb,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int argb) {
        v(bb, x1, y1, z1, argb);
        v(bb, x2, y2, z2, argb);
        v(bb, x3, y3, z3, argb);
        v(bb, x4, y4, z4, argb);
    }

    private static void storeMaskedBlock(BufferBuilder bb,
                                         float x, float y, float z,
                                         int argb, byte mask) {
        if ((mask & (1)) != 0) // +X
            quad(bb, x + 1, y, z + 1, x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, argb);
        if ((mask & (1 << 1)) != 0) // -X
            quad(bb, x, y, z, x, y, z + 1, x, y + 1, z + 1, x, y + 1, z, argb);
        if ((mask & (1 << 2)) != 0) // +Y
            quad(bb, x, y + 1, z, x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, argb);
        if ((mask & (1 << 3)) != 0) // -Y
            quad(bb, x, y, z + 1, x, y, z, x + 1, y, z, x + 1, y, z + 1, argb);
        if ((mask & (1 << 4)) != 0) // +Z
            quad(bb, x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1, argb);
        if ((mask & (1 << 5)) != 0) // -Z
            quad(bb, x + 1, y, z, x, y, z, x, y + 1, z, x + 1, y + 1, z, argb);
    }
}
