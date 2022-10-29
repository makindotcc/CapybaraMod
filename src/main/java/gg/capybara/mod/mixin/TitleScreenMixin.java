//package gg.capybara.mod.mixin;
//
//import gg.capybara.mod.CapybaraMod;
//import net.minecraft.client.gui.screen.TitleScreen;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(TitleScreen.class)
//public class TitleScreenMixin {
//    @Inject(at = @At("HEAD"), method = "init()V")
//    private void init(CallbackInfo info) {
//        CapybaraMod.LOGGER.info("This line is printed by an capybara mod mixin!");
//        CapybaraMod.test();
//    }
//}
