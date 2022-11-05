package gg.capybara.mod.optimization.font

import com.mojang.blaze3d.vertex.VertexBuffer
import net.minecraft.client.renderer.RenderType

sealed interface FontCache {
    object TooYoung : FontCache
    data class Established(
        val vbo: List<Pair<RenderType, VertexBuffer>>,
        val textWidth: Int,
    ) : FontCache
}