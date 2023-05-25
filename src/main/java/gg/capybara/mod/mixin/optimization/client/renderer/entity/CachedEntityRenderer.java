package gg.capybara.mod.mixin.optimization.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public abstract class CachedEntityRenderer {
    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract Font getFont();

    // hmm, we should probably pack full entity instead caching only name tag
    // TODO
//    /**
//     * @author www_makin_cc
//     * @reason Optimize nametag rendering.
//     */
//    @Overwrite
//    public void renderNameTag(Entity entity, Component component, PoseStack poseStack,
//            MultiBufferSource multiBufferSource, int i) {
//        double d = this.entityRenderDispatcher.distanceToSqr(entity);
//        if (!(d > 4096.0)) {
//            boolean renderThroughWalls = !entity.isDiscrete();
//            float posY = entity.getBbHeight() + 0.5F;
//            int offsetY = "deadmau5".equals(component.getString()) ? -10 : 0;
//            poseStack.pushPose();
//            poseStack.translate(0.0, posY, 0.0);
//            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
//            poseStack.scale(-0.025F, -0.025F, 0.025F);
//            Matrix4f matrix4f = poseStack.last().pose();
//            float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
//            int backgroundColor = (int) (opacity * 255.0F) << 24;
//            Font font = this.getFont();
//            float offsetX = (float) (-font.width(component) / 2);
////            font.draw(poseStack, component, offsetX, offsetY, i);
////            if (renderThroughWalls) {
////
////            }
////            font.drawInBatch(component, offsetX, (float) offsetY, 0x20FFFFFF, false, matrix4f, multiBufferSource,
////                    renderThroughWalls, backgroundColor, i);
////            if (renderThroughWalls) {
////                font.drawInBatch(component, offsetX, (float) offsetY, -1, false, matrix4f,
////                        multiBufferSource, false, 0, i);
////            }
//
//            poseStack.popPose();
//        }
//    }
}
