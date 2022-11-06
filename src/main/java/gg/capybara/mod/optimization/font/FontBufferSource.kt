package gg.capybara.mod.optimization.font

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.VertexBuffer
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType

class FontBufferSource(
    private val bufferBuilder: BufferBuilder,
) : MultiBufferSource {
    private val compiledBuffers = ArrayList<Pair<RenderType, VertexBuffer>>()
    private var lastState: RenderType? = null
    private var startedBuffer = false

    override fun getBuffer(renderType: RenderType): VertexConsumer {
        if (this.lastState != renderType || !renderType.canConsolidateConsecutiveGeometry()) {
            if (this.lastState != null) {
                this.endBatch(renderType)
            }

            if (!this.startedBuffer) {
                this.startedBuffer = true
                this.bufferBuilder.begin(renderType.mode(), renderType.format())
            }

            this.lastState = renderType
        }
        return this.bufferBuilder
    }

    fun endBatch(): List<Pair<RenderType, VertexBuffer>> {
        this.lastState?.let { lastState ->
            this.endBatch(lastState)
        }
        return this.compiledBuffers
    }

    private fun endBatch(renderType: RenderType) {
        if (this.lastState == renderType) {
            this.startedBuffer = false
            if (this.bufferBuilder.building()) {
                this.bufferBuilder.setQuadSortOrigin(0f, 0f, 0f)

                val renderedBuffer = this.bufferBuilder.end()
                renderType.setupRenderState()
                val vertexBuffer = VertexBuffer()
                vertexBuffer.bind()
                vertexBuffer.upload(renderedBuffer)
                vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(),
                    RenderSystem.getShader()!!)
                renderType.clearRenderState()

                this.compiledBuffers.add(Pair(renderType, vertexBuffer))
            }
            this.lastState = null
        }
    }
}