package gg.capybara.mod.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.obfuscate.DontObfuscate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class CapybaraBrandRetriever {
    @DontObfuscate
    @Overwrite(remap = false)
    public static String getClientModName() {
        return "CapybaraMod";
    }
}
