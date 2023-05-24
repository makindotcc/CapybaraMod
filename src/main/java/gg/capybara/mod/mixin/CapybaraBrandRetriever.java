package gg.capybara.mod.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.obfuscate.DontObfuscate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class CapybaraBrandRetriever {
    /**
     * @author www_makin_cc
     * @reason Replace "Fabric" brand message sent to server with "vanilla" one - some servers cry about it, because
     * they think it will help prevent joining cheaters using fabric based cheats 🤣💀.
     */
    @DontObfuscate
    @Overwrite(remap = false)
    public static String getClientModName() {
        return "vanilla";
    }
}
