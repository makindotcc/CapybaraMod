package gg.capybara.mod.mixin.optimization.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import gg.capybara.mod.VertexCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(Font.class)
public abstract class FontMixin {
    private Map<Pair<FormattedCharSequence, Float>, VertexCache> cachedDrawInternal = new HashMap<>();

    @Shadow
    public abstract int drawInBatch(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k);

    // todo:
    // - memoizacja wszystkich argumentow
    // - ignore jak tekst jest obfuscated &k
    // - usuwac vbo jak nieaktywne
    @Overwrite
    private int drawInternal(FormattedCharSequence charSeq, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
        Pair<FormattedCharSequence, Float> key = Pair.of(charSeq, g);
        VertexCache t = cachedDrawInternal.get(key);
        if (t == null) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance()
                .getBuilder());
            int j = this.drawInBatch(charSeq, f, g, i, bl, matrix4f, bufferSource, false, 0, 15728880);
            BufferBuilder builder = ((BufferSourceAccessor) bufferSource).getBuilder();
            if (!builder.building()) {
                return j;
            }
            RenderType renderType = ((BufferSourceAccessor) bufferSource).getLastState().get();

            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            renderType.setupRenderState();

            VertexBuffer vertexBuffer = new VertexBuffer();
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

            renderType.clearRenderState();

            t = new VertexCache(vertexBuffer, j, renderType);
            cachedDrawInternal.put(key, t);
        } else {
            t.renderType.setupRenderState();
            t.vertexBuffer.bind();
            t.vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
            t.renderType.clearRenderState();
        }

        return t.returnValue;
    }
}