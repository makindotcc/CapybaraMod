package gg.capybara.mod.mixin.optimization.player;

import gg.capybara.mod.CapybaraMod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(at = @At("HEAD"), method = "sendCommand")
    private void init(String command, Component preview, CallbackInfo ci) {
        CapybaraMod.LOGGER.info("On send command: " + command);
    }
}
