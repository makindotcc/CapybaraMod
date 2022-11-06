package gg.capybara.mod.mixin.event;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.capybara.mod.event.HudRenderCallback;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHook {
    // Source: fabric-rendering-v1 - we cannot use fabric-rendering-v1, because it is not compatible with sodium yet.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V")))
    public void render(PoseStack matrixStack, float tickDelta, CallbackInfo callbackInfo) {
        HudRenderCallback.EVENT.invoker().onHudRender(matrixStack, tickDelta);
    }
}
