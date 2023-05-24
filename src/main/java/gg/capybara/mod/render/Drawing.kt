package gg.capybara.mod.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ShaderInstance
import org.joml.Matrix4f

object Drawing {
    fun blit(
        poseStack: PoseStack, i: Int, j: Int, k: Int, f: Float, g: Float, l: Int, m: Int, n: Int, o: Int,
        shader: ShaderInstance? = GameRenderer.getPositionTexShader(),
    ) {
        innerBlit(poseStack, i, i + l, j, j + m, k, l, m, f, g, n, o, shader = shader)
    }

    private fun innerBlit(
        poseStack: PoseStack, i: Int, j: Int, k: Int, l: Int, m: Int, n: Int, o: Int,
        f: Float, g: Float, p: Int, q: Int,
        shader: ShaderInstance? = GameRenderer.getPositionTexShader(),
    ) {
        innerBlit(
            poseStack.last().pose(), i, j, k, l, m, (f + 0.0f) / p.toFloat(), (f + n.toFloat()) / p.toFloat(),
            (g + 0.0f) / q.toFloat(), (g + o.toFloat()) / q.toFloat(),
            shader = shader,
        )
    }

    private fun innerBlit(
        matrix4f: Matrix4f, i: Int, j: Int, k: Int, l: Int, m: Int, f: Float, g: Float,
        h: Float, n: Float,
        shader: ShaderInstance? = GameRenderer.getPositionTexShader(),
    ) {
        RenderSystem.setShader { shader }
        val bufferBuilder = Tesselator.getInstance().builder
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        bufferBuilder.vertex(matrix4f, i.toFloat(), l.toFloat(), m.toFloat()).uv(f, n).endVertex()
        bufferBuilder.vertex(matrix4f, j.toFloat(), l.toFloat(), m.toFloat()).uv(g, n).endVertex()
        bufferBuilder.vertex(matrix4f, j.toFloat(), k.toFloat(), m.toFloat()).uv(g, h).endVertex()
        bufferBuilder.vertex(matrix4f, i.toFloat(), k.toFloat(), m.toFloat()).uv(f, h).endVertex()
        BufferUploader.drawWithShader(bufferBuilder.end())
    }
}