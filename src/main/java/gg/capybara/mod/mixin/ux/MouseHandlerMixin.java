package gg.capybara.mod.mixin.ux;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow private boolean mouseGrabbed;

    @Inject(at = @At("HEAD"), method = "releaseMouse", cancellable = true)
    public void stealCursor(CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ReceivingLevelScreen || screen instanceof ProgressScreen) {
            ci.cancel();
            this.mouseGrabbed = false;
        }
    }
}
