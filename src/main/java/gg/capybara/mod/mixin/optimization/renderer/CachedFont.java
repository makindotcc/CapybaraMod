package gg.capybara.mod.mixin.optimization.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import gg.capybara.mod.optimization.CachedBufferSource;
import gg.capybara.mod.optimization.font.CharSeqHashCodeCalculator;
import gg.capybara.mod.optimization.font.FontCache;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Caches rendered text vertex buffer.
 */
@Mixin(Font.class)
public abstract class CachedFont {
    private static final long OBFUSCATED_FPS = 60;

    private final Cache<Object, FontCache> drawCache = CacheBuilder.newBuilder()
            .expireAfterAccess(500, TimeUnit.MILLISECONDS)
            .removalListener((notification) -> {
                FontCache fontCache = ((FontCache) notification.getValue());
                if (fontCache instanceof FontCache.Established established) {
                    for (Pair<RenderType, VertexBuffer> entry : established.getVbo()) {
                        entry.getSecond().close();
                    }
                }
            })
            .build();

    @Shadow
    public abstract int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f,
            MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k, boolean bl2);

    @Shadow
    public abstract int drawInBatch(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl,
            Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k);

//    @Inject(method = "drawInternal(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I", at = @At("HEAD"),
//            cancellable = true)
//    private void drawCached(String text, float x, float y, int color, Matrix4f translation,
//            boolean shadow, boolean rightToLeft, CallbackInfoReturnable<Integer> cir
//    ) {
//        if (text == null) {
//            return;
//        }
//
//        Object cacheKey = new Pair<>(text, color);
//        FontCache fontCache = this.drawCache.getIfPresent(cacheKey);
//        if (fontCache == null) {
//            this.drawCache.put(cacheKey, FontCache.TooYoung.INSTANCE);
//        } else if (fontCache instanceof FontCache.TooYoung) {
//            Function<MultiBufferSource, Integer> draw = (bufferSource) ->
//                    this.drawInBatch(text, 0, 0, color, shadow, new Matrix4f(),
//                            bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0, rightToLeft);
//            FontCache.Established established = establishCache(x, y, Long.MAX_VALUE, translation, draw);
//            this.drawCache.put(cacheKey, established);
//            cir.setReturnValue(established.getTextWidth());
//        } else if (fontCache instanceof FontCache.Established established) {
//            drawCacheEstablished(established, x, y, translation);
//            cir.setReturnValue(established.getTextWidth());
//        }
//    }
//
//    @Inject(method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFILorg/joml/Matrix4f;Z)I",
//            at = @At("HEAD"), cancellable = true)
//    public void drawCached(FormattedCharSequence text, float x, float y, int color, Matrix4f translation,
//            boolean shadow, CallbackInfoReturnable<Integer> cir
//    ) {
//        int cacheKey = 31 * color + CharSeqHashCodeCalculator.calculate(text);
//        FontCache fontCache = this.drawCache.getIfPresent(cacheKey);
//        if (fontCache == null) {
//            this.drawCache.put(cacheKey, FontCache.TooYoung.INSTANCE);
//        } else if (fontCache instanceof FontCache.TooYoung) {
//            long timeoutAt = isObfuscated(text) ?
//                    System.currentTimeMillis() + (1000 / OBFUSCATED_FPS)
//                    :
//                    Long.MAX_VALUE;
//            Function<MultiBufferSource, Integer> draw = (bufferSource) ->
//                    this.drawInBatch(text, 0, 0, color, shadow, new Matrix4f(),
//                            bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
//            FontCache.Established established = establishCache(x, y, timeoutAt, translation, draw);
//            this.drawCache.put(cacheKey, established);
//            cir.setReturnValue(established.getTextWidth());
//        } else if (fontCache instanceof FontCache.Established established) {
//            drawCacheEstablished(established, x, y, translation);
//            cir.setReturnValue(established.getTextWidth());
//            if (established.getTimeoutAt() != Long.MAX_VALUE &&
//                    System.currentTimeMillis() >= established.getTimeoutAt()
//            ) {
//                this.drawCache.put(cacheKey, FontCache.TooYoung.INSTANCE);
//            }
//        }
//    }

    private static FontCache.Established establishCache(float x, float y, long timeoutAt, Matrix4f translation,
            Function<MultiBufferSource, Integer> drawText
    ) {
        CachedBufferSource bufferSource = new CachedBufferSource(Tesselator.getInstance().getBuilder(),
                ImmutableMap.of());

        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f previousModelViewMatrix = new Matrix4f(modelViewMatrix);
        applyTranslation(x, y, translation, modelViewMatrix);

        int textWidth = (int) (x + drawText.apply(bufferSource));
        List<Pair<RenderType, VertexBuffer>> vboList = bufferSource.endBatch();

        modelViewMatrix.set(previousModelViewMatrix);
        return new FontCache.Established(vboList, textWidth, timeoutAt);
    }

    private static void drawCacheEstablished(FontCache.Established established, float x, float y,
            Matrix4f translation
    ) {
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Matrix4f previousModelViewMatrix = new Matrix4f(modelViewMatrix);
        applyTranslation(x, y, translation, modelViewMatrix);

        for (Pair<RenderType, VertexBuffer> entry : established.getVbo()) {
            RenderType renderType = entry.getFirst();
            VertexBuffer vbo = entry.getSecond();

            vbo.bind();
            renderType.setupRenderState();
            vbo.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
            renderType.clearRenderState();
        }

        modelViewMatrix.set(previousModelViewMatrix);
    }

    private static boolean isObfuscated(FormattedCharSequence text) {
        return !text.accept((int i, Style style, int j) -> !style.isObfuscated());
    }

    private static void applyTranslation(float x, float y, Matrix4f matrix4f, Matrix4f modelViewMatrix) {
        Matrix4f translation = new Matrix4f(matrix4f);
        translation.mul(new Matrix4f().translate(x, y, 0f));
        modelViewMatrix.mul(translation);
    }
}