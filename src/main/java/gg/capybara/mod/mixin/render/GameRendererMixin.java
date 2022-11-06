package gg.capybara.mod.mixin.render;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import gg.capybara.mod.render.CapyShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
        method = "reloadShaders",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void addCapyShaders(ResourceManager resourceManager, CallbackInfo ci,
        List<Program> ignored, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaders
    ) throws IOException {
        System.out.println("ESSSSSA");
        ShaderInstance logoText = new ShaderInstance(resourceManager, "capybara_logo",
            DefaultVertexFormat.POSITION_TEX);
        shaders.add(Pair.of(logoText, CapyShaders.INSTANCE::setRendertypeLogoText));
    }
}
