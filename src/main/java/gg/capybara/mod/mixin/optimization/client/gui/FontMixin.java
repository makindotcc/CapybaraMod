package gg.capybara.mod.mixin.optimization.client.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import gg.capybara.mod.optimization.font.FontBufferSource;
import gg.capybara.mod.optimization.font.FontCache;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Mixin(Font.class)
public abstract class FontMixin {
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
    public abstract int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k, boolean bl3);

    @Inject(method = "drawInternal(Ljava/lang/String;FFILcom/mojang/math/Matrix4f;ZZ)I", at = @At("HEAD"),
        cancellable = true)
    private void drawInternal(String text, float x, float y, int color, Matrix4f matrix4f,
        boolean shadow, boolean rightToLeft, CallbackInfoReturnable<Integer> cir
    ) {
        if (text == null) {
            return;
        }

        FontCache fontCache = this.drawCache.getIfPresent(text);
        if (fontCache == null) {
            this.drawCache.put(text, FontCache.TooYoung.INSTANCE);
        } else if (fontCache instanceof FontCache.TooYoung) {
            FontBufferSource bufferSource = new FontBufferSource(Tesselator.getInstance().getBuilder());
            int textWidth = this.drawInBatch(text, x, y, color, shadow, matrix4f, bufferSource,
                false, 0, 0xF000F0, rightToLeft);
            List<Pair<RenderType, VertexBuffer>> vboList = bufferSource.endBatch();
            this.drawCache.put(text, new FontCache.Established(vboList, textWidth));

            cir.setReturnValue(textWidth);
        } else if (fontCache instanceof FontCache.Established established) {
            Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
            Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
            for (Pair<RenderType, VertexBuffer> entry : established.getVbo()) {
                RenderType renderType = entry.getFirst();
                VertexBuffer vbo = entry.getSecond();

                vbo.bind();
                renderType.setupRenderState();
                vbo.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                renderType.clearRenderState();
            }

            cir.setReturnValue(established.getTextWidth());
        }
    }
}