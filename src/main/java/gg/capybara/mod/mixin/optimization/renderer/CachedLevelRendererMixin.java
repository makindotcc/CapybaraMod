package gg.capybara.mod.mixin.optimization.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.capybara.mod.optimization.font.CachedLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class CachedLevelRendererMixin {
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    /**
     * @author test
     * @reason test
     */
    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void renderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource, CallbackInfo ci) {
        CachedLevelRenderer.renderEntity(entity, d, e, f, g, poseStack, this.entityRenderDispatcher, ci);
    }
}
