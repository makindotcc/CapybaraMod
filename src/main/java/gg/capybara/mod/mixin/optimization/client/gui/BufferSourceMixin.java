package gg.capybara.mod.mixin.optimization.client.gui;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class BufferSourceMixin {
    public final List<Pair<VertexBuffer, RenderType>> vbos = new ArrayList<>();
//
//    @Shadow
//    @Final
//    protected BufferBuilder builder;
//
//    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V",
//        at = @At(value = "INVOKE_ASSIGN",
//            target = "Lnet/minecraft/client/renderer/RenderType;end(Lcom/mojang/blaze3d/vertex/BufferBuilder;III)V"))
//    public void onRenderTypeEnd(RenderType renderType, CallbackInfo ci) {
//        if (FontRenderer.RENDERING_FONT) {
//            VertexBuffer vb = new VertexBuffer();
//            vb.bind();
//            vb.upload(this.builder.end());
//            this.vbos.add(Pair.of(vb, renderType));
//        }
//    }
}
