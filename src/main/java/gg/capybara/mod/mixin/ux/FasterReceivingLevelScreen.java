package gg.capybara.mod.mixin.ux;

import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReceivingLevelScreen.class)
public class FasterReceivingLevelScreen {
    @Shadow
    private boolean oneTickSkipped;

    @Inject(at = @At("HEAD"), method = "loadingPacketsReceived")
    public void tick(final CallbackInfo ci) {
        this.oneTickSkipped = true;
    }
}
