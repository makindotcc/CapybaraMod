package gg.capybara.mod.mixin.optimization.client.gui;

import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class StringRenderOutputMixin {
    @Mutable
    @Shadow
    @Final
    private MultiBufferSource bufferSource;

//    @Inject(at = @At("TAIL"), method = "<init>(" +
//        "Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource;" +
//        "FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/gui/Font$DisplayMode;I)V")
//    public void ctor(CallbackInfo callbackInfo) {
//        this.bufferSource = MultiBufferSource.immediate(Tesselator.getInstance()
//            .getBuilder());
//    }
}
