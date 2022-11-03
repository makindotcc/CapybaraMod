package gg.capybara.mod.optimization;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.datafixers.util.Function5;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import gg.capybara.mod.VertexCache;
import gg.capybara.mod.mixin.optimization.client.gui.BufferSourceAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class FontRenderer {
    @Nullable
    private static Object cacheKey;

    @Nullable
    public static Object getCacheKey() {
        return cacheKey;
    }

    public static void setCacheKey(@Nullable Object newKey) {
//        System.out.println("cache key:\n" + newKey);
        cacheKey = newKey;
    }

    public static boolean RENDERING_FONT = false;
    private final Cache<FormattedCharSequence, VertexCache> cachedDrawInternal = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.SECONDS)
        .removalListener((notification) -> {
            VertexCache cached = (VertexCache) notification.getValue();
            assert cached != null;
            cached.vertexBuffer.close();
        })
        .build();
    private final Function5<FormattedCharSequence, Integer, Boolean, Matrix4f, MultiBufferSource.BufferSource, Integer> drawInBatch;

    public FontRenderer(Function5<FormattedCharSequence, Integer, Boolean, Matrix4f, MultiBufferSource.BufferSource, Integer> drawInBatch) {
        this.drawInBatch = drawInBatch;
    }

    // todo:
    // - memoizacja scalematrix i bl (nwm co to te bl)
    // - ignore jak tekst jest obfuscated &k
    // - na czacie jak wiadomosc znika to opacity tekstu sie nie zmienia
    // - ten pierdolec jest taki ze ruzne litery maja rozne rendertype i
    //   trzeba rozne vbo do tych rendertype roznych dac
    public int drawInternal(FormattedCharSequence charSeq, float x, float y, int color, Matrix4f scaleMatrix, boolean bl) {
        FormattedCharSequence key = charSeq;
        VertexCache cache = cachedDrawInternal.getIfPresent(charSeq);
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Matrix4f previousMatrix = modelViewMatrix.copy();
        modelViewMatrix.translate(new Vector3f(x, y, 0));

        try {
            if (cache == null || cache.isDead()) {
                BufferBuilder builder = Tesselator.getInstance().getBuilder();
                System.out.println("START Kurwa");
                RENDERING_FONT = true;
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(builder);
                int textWidth = this.drawInBatch.apply(charSeq, color, bl, scaleMatrix, bufferSource);
                if (!builder.building()) {
                    return textWidth + (int) x;
                }

                if (true) {
                    builder.end();
                    RENDERING_FONT = false;
                    System.out.println("stop Kurwa");
                    return textWidth + (int) x;
                }
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                RenderType renderType = ((BufferSourceAccessor) bufferSource).getLastState().get();

                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();

                VertexBuffer vertexBuffer = new VertexBuffer();
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                vertexBuffer.bind();

//                renderType.setupRenderState();
//                vertexBuffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
//                renderType.clearRenderState();

                VertexBuffer.unbind();

                boolean cached = charSeq.accept((i, style, j) -> !style.isObfuscated());
                cache = new VertexCache(vertexBuffer, textWidth + (int) x, renderType,
                    cached ? Long.MAX_VALUE : System.currentTimeMillis() + 500);
                cachedDrawInternal.put(key, cache);
            } else {
                cache.vertexBuffer.bind();
                cache.renderType.setupRenderState();
                cache.vertexBuffer.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                cache.renderType.clearRenderState();

                VertexBuffer.unbind();
            }
        } finally {
            modelViewMatrix.load(previousMatrix);
        }

        return cache.textWidth;
    }
}
