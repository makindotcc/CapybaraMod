package gg.capybara.mod.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface HudRenderCallback {
    Event<HudRenderCallback> EVENT = EventFactory.createArrayBacked(HudRenderCallback.class, (listeners) -> (matrixStack, delta) -> {
        for (HudRenderCallback event : listeners) {
            event.onHudRender(matrixStack, delta);
        }
    });

    /**
     * Called after rendering the whole hud, which is displayed in game, in a world.
     *
     * @param matrixStack the matrixStack
     * @param tickDelta Progress for linearly interpolating between the previous and current game state
     */
    void onHudRender(PoseStack matrixStack, float tickDelta);
}