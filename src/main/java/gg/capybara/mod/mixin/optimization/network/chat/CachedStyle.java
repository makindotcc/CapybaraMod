package gg.capybara.mod.mixin.optimization.network.chat;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class CachedStyle {
    private int hashCode;

    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    public void returnCachedHashCode(CallbackInfoReturnable<Integer> cir) {
        if (this.hashCode != 0) {
            cir.setReturnValue(this.hashCode);
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"))
    public void cacheHashCode(CallbackInfoReturnable<Integer> cir) {
        this.hashCode = cir.getReturnValueI();
    }
}
