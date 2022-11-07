package gg.capybara.mod.mixin.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import gg.capybara.mod.render.ShaderInstanceExt;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderInstance.class)
public abstract class CapyShaderInstance implements ShaderInstanceExt {

    public Uniform PROGRAM_TIME;

    @Shadow
    public abstract Uniform getUniform(String string);

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void constructor(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat, CallbackInfo ci) {
        PROGRAM_TIME = this.getUniform("ProgramTime");
    }

    @Override
    public Uniform getProgramTimeUniform() {
        return PROGRAM_TIME;
    }
}
