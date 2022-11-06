package gg.capybara.mod.mixin.ux;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Inject(at = @At("TAIL"), method = "setScreen")
    public void setScreen(Screen screen, CallbackInfo ci) {
        if (screen == null && Minecraft.ON_OSX) {
            KeyMapping.setAll();
        }
    }
}
