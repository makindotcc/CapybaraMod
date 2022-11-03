package gg.capybara.mod.mixin.optimization.client.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import gg.capybara.mod.CapybaraMod;
import gg.capybara.mod.optimization.FontRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class StringRenderOutputMixin {
    private static final Cache<Object, List<Pair<VertexBuffer, RenderType>>> cachedVbo = CacheBuilder.newBuilder()
        .expireAfterAccess(100, TimeUnit.MILLISECONDS)
        .removalListener((notification) -> {
            @SuppressWarnings("unchecked")
            List<Pair<VertexBuffer, RenderType>> cached = (List<Pair<VertexBuffer, RenderType>>) notification.getValue();
            assert cached != null;
            for (Pair<VertexBuffer, RenderType> entry : cached) {
                entry.getLeft().close();
            }
        })
        .build();

    @Mutable
    @Shadow
    @Final
    private int packedLightCoords;

    private RenderType currentRenderType;
    private final List<Pair<VertexBuffer, RenderType>> compiledBuffers = new ArrayList<>();
    private List<Pair<VertexBuffer, RenderType>> cached;

    @Inject(method = "<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource;" +
        "FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/gui/Font$DisplayMode;I)V", at = @At("TAIL"))
    public void ctor(Font font, MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl,
        Matrix4f matrix4f, Font.DisplayMode displayMode, int j, CallbackInfo ci
    ) {
        Object cacheKey = FontRenderer.getCacheKey();
        if (cacheKey != null) {
            this.cached = cachedVbo.getIfPresent(cacheKey);
            Tesselator.getInstance().getBuilder().discard();
        }
    }

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    public void accept(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        if (this.cached != null) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
        method = "accept",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(" +
                "Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    public VertexConsumer acceptGetBuffer(MultiBufferSource instance, RenderType renderType) {
        if (FontRenderer.getCacheKey() != null) {
            this.packedLightCoords = 0x220088;
        }
        return this.getCapyBuffer(renderType);
    }

    @Redirect(
        method = "finish",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(" +
                "Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    public VertexConsumer finishGetBuffer(MultiBufferSource instance, RenderType renderType) {
        return this.getCapyBuffer(renderType);
    }

    @Inject(method = "finish", at = @At("HEAD"), cancellable = true)
    public void finishUseCache(int i, float f, CallbackInfoReturnable<Float> cir) {
        if (this.cached != null) {
            Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
            Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
            for (Pair<VertexBuffer, RenderType> entry : this.cached) {
                VertexBuffer vbo = entry.getLeft();
                RenderType renderType = entry.getRight();

                vbo.bind();
                renderType.setupRenderState();
                vbo.drawWithShader(modelViewMatrix, projectionMatrix, RenderSystem.getShader());
                renderType.clearRenderState();
                VertexBuffer.unbind();
            }

            cir.setReturnValue(200f);
        }
    }

    @Inject(method = "finish", at = @At("TAIL"))
    public void finishBuildVbo(int i, float f, CallbackInfoReturnable<Float> cir) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        // fast path
        if (this.compiledBuffers.size() == 0) {
            VertexBuffer vbo = new VertexBuffer();
            vbo.bind();
            vbo.upload(bufferBuilder.end());

            this.currentRenderType.setupRenderState();
            vbo.drawWithShader(RenderSystem.getModelViewMatrix(),
                RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
            this.currentRenderType.clearRenderState();
            VertexBuffer.unbind();

            Object cacheKey = FontRenderer.getCacheKey();
            if (cacheKey != null) {
                cachedVbo.put(cacheKey, Collections.singletonList(Pair.of(vbo, this.currentRenderType)));
            } else {
                vbo.close();
            }
        } else {
            this.finishVbo(bufferBuilder);
            for (Pair<VertexBuffer, RenderType> entry : this.compiledBuffers) {
                VertexBuffer vbo = entry.getLeft();
                RenderType renderType = entry.getRight();

                vbo.bind();
                renderType.setupRenderState();
                vbo.drawWithShader(RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                renderType.clearRenderState();
                VertexBuffer.unbind();
            }

            Object cacheKey = FontRenderer.getCacheKey();
            if (cacheKey != null) {
                cachedVbo.put(cacheKey, this.compiledBuffers);
            } else {
                for (Pair<VertexBuffer, RenderType> cached : this.compiledBuffers) {
                    cached.getLeft().close();
                }
            }
        }
    }

    @NotNull
    private BufferBuilder getCapyBuffer(RenderType renderType) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        if (this.currentRenderType != renderType) {
            this.finishVbo(bufferBuilder);
            this.currentRenderType = renderType;
            bufferBuilder.begin(renderType.mode(), renderType.format());
        }
        return bufferBuilder;
    }

    private void finishVbo(BufferBuilder bufferBuilder) {
        if (this.currentRenderType != null) {
            VertexBuffer vbo = new VertexBuffer();
            vbo.bind();
            vbo.upload(bufferBuilder.end());
            VertexBuffer.unbind();

            this.compiledBuffers.add(Pair.of(vbo, this.currentRenderType));
        }
    }
}
