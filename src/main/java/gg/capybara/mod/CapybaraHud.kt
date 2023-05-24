package gg.capybara.mod

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import gg.capybara.mod.render.CapyShaders
import gg.capybara.mod.render.Drawing
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.atomic.AtomicBoolean

class CapybaraHud(
    private val watermarkEnabled: AtomicBoolean,
) {
    fun render(matrixStack: PoseStack, delta: Float) {
        val minecraft = Minecraft.getInstance()
        if (minecraft.options.renderDebug) {
            return
        }
        if (watermarkEnabled.get()) {
            renderWatermark(matrixStack)
        }
    }

    private fun renderWatermark(matrixStack: PoseStack) {
        val logoText = CapyShaders.rendertypeLogoText
            ?: return
        RenderSystem.setShaderTexture(0, LOGO_LOCATION)
        val size = 100
        // jak najezdza na tlo czarne typu czat to wtedy rozjasnia je wiec nwm todo fix ?
        RenderSystem.enableBlend()
        Drawing.blit(matrixStack, 7, 7, 0, 0f, 10f, size, size - 10, size, size, shader = logoText)
        RenderSystem.disableBlend()
    }

    companion object {
        private val LOGO_LOCATION = ResourceLocation("capybara", "logo.png")
    }
}
