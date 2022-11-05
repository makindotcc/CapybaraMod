package gg.capybara.mod.optimization.font

import com.mojang.blaze3d.vertex.VertexBuffer
import net.minecraft.client.renderer.RenderType

sealed interface FontCache {
    // Do not try to cache texts that are rendered too short.
    object TooYoung : FontCache
    data class Established(
        val vbo: List<Pair<RenderType, VertexBuffer>>,
        val textWidth: Int,
        // Unix timestamp
        val timeoutAt: Long,
    ) : FontCache
}