package gg.capybara.mod.mixin.blaze3d.vertex;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.joml.Matrix4f;
import gg.capybara.mod.render.ShaderInstanceExt;
import net.minecraft.Util;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VertexBuffer.class)
public class MixinVertexBuffer {

    @Inject(
            method = "_drawWithShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setupShaderLights(Lnet/minecraft/client/renderer/ShaderInstance;)V"
            )
    )
    public void _drawWithShaderHook(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance, CallbackInfo ci) {
        ShaderInstanceExt extendedShaderInstance = (ShaderInstanceExt) shaderInstance;
        Uniform timeUniform = extendedShaderInstance.getProgramTimeUniform();

        if (timeUniform != null) {
            timeUniform.set((float) (Util.getMillis() % 21_600_000 / 1000.0)); // Resets every 6 hours
        }
    }


}
