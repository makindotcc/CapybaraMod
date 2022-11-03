package gg.capybara.mod.mixin.optimization.client.gui;

import com.mojang.math.Matrix4f;
import gg.capybara.mod.optimization.FontRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public abstract class FontMixin {
    private final FontRenderer fontRenderer = new FontRenderer(((charSeq, i, bl, matrix4f, bufferSource) ->
        this.drawInBatch(charSeq, 0, 0, i, bl, matrix4f, bufferSource, false, 0, 0xF000F0)));

    @Shadow
    public abstract int drawInBatch(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl,
        Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k);

    @Inject(
        method = "renderText(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("HEAD")
    )
    public void enableCacheStr(String text, float f, float g, int i, boolean bl, Matrix4f matrix4f,
        MultiBufferSource multiBufferSource, boolean bl2, int j, int k, CallbackInfoReturnable<Float> cir
    ) {
        FontRenderer.setCacheKey(text);
    }

    @Inject(
        method = "renderText(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("TAIL")
    )
    public void disableCacheStr(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f,
        MultiBufferSource multiBufferSource, boolean bl2, int j, int k, CallbackInfoReturnable<Float> cir
    ) {
        FontRenderer.setCacheKey(null);
    }

    @Inject(
        method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("HEAD")
    )
    public void enableCacheSeq(FormattedCharSequence text, float f, float g, int i, boolean bl, Matrix4f matrix4f,
        MultiBufferSource multiBufferSource, boolean bl2, int j, int k, CallbackInfoReturnable<Float> cir
    ) {
        FontRenderer.setCacheKey(text);
    }

    @Inject(
        method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F",
        at = @At("TAIL")
    )
    public void disableCacheSeq(FormattedCharSequence string, float f, float g, int i, boolean bl, Matrix4f matrix4f,
        MultiBufferSource multiBufferSource, boolean bl2, int j, int k, CallbackInfoReturnable<Float> cir
    ) {
        FontRenderer.setCacheKey(null);
    }

//    @Overwrite
//    private int drawInternal(FormattedCharSequence charSeq, float x, float y, int i, Matrix4f matrix4f, boolean bl) {
//        return this.fontRenderer.drawInternal(charSeq, x, y, i, matrix4f, bl);
//    }
}