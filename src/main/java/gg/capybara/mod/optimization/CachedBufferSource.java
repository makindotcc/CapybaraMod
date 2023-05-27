package gg.capybara.mod.optimization;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kotlin.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Fork of {@link MultiBufferSource.BufferSource}.
 * Renders to newly created {@link VertexBuffer} and saves them in {@link #buffers}.
 */
public class CachedBufferSource implements MultiBufferSource {
    protected final BufferBuilder builder;
    protected final Map<RenderType, BufferBuilder> fixedBuffers;
    protected Optional<RenderType> lastState = Optional.empty();
    protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();
    private final List<Pair<RenderType, VertexBuffer>> buffers = new ArrayList<>();

    public CachedBufferSource(BufferBuilder bufferBuilder, Map<RenderType, BufferBuilder> map) {
        this.builder = bufferBuilder;
        this.fixedBuffers = map;
    }

    public @NotNull VertexConsumer getBuffer(RenderType renderType) {
        Optional<RenderType> optional = renderType.asOptional();
        BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
        if (!Objects.equals(this.lastState, optional) || !renderType.canConsolidateConsecutiveGeometry()) {
            if (this.lastState.isPresent()) {
                RenderType renderType2 = (RenderType) this.lastState.get();
                if (!this.fixedBuffers.containsKey(renderType2)) {
                    VertexBuffer buffer = this.endBatch(renderType2);
                    if (buffer != null) {
                        buffers.add(new Pair<>(renderType2, buffer));
                    }
                }
            }

            if (this.startedBuffers.add(bufferBuilder) && !bufferBuilder.building()) {
                bufferBuilder.begin(renderType.mode(), renderType.format());
            }

            this.lastState = optional;
        }

        return bufferBuilder;
    }

    private BufferBuilder getBuilderRaw(RenderType renderType) {
        return this.fixedBuffers.getOrDefault(renderType, this.builder);
    }

    public List<Pair<RenderType, VertexBuffer>> endBatch() {
        this.lastState.ifPresent((renderTypex) -> {
            VertexConsumer vertexConsumer = this.getBuffer(renderTypex);
            if (vertexConsumer == this.builder) {
                VertexBuffer buffer = this.endBatch(renderTypex);
                if (buffer != null) {
                    this.buffers.add(new Pair<>(renderTypex, buffer));
                }
            }

        });
        for (RenderType renderType : this.fixedBuffers.keySet()) {
            VertexBuffer buffer = this.endBatch(renderType);
            if (buffer != null) {
                this.buffers.add(new Pair<>(renderType, buffer));
            }
        }
        return this.buffers;
    }

    @Nullable
    public VertexBuffer endBatch(RenderType renderType) {
        BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
        boolean bl = Objects.equals(this.lastState, renderType.asOptional());
        VertexBuffer vertexBuffer = null;
        if (bl || bufferBuilder != this.builder) {
            if (this.startedBuffers.remove(bufferBuilder) && this.builder.building()) {
                BufferBuilder.RenderedBuffer renderedBuffer = this.builder.end();
                renderType.setupRenderState();
                vertexBuffer = new VertexBuffer();
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(),
                        RenderSystem.getShader());
                renderType.clearRenderState();

                if (bl) {
                    this.lastState = Optional.empty();
                }

            }
        }
        return vertexBuffer;
    }
}
