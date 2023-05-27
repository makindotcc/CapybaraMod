package gg.capybara.mod.optimization.font

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexBuffer
import gg.capybara.mod.optimization.CachedBufferSource
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.player.Player
import org.joml.Matrix4f
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.io.Closeable
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

object CachedLevelRenderer {
    private val CACHE_TIMEOUT = Duration.ofSeconds(1)
    private val compiledEntities = HashMap<UUID, CachedEntity>()
    private var lastEntitiesCleanupAt = System.currentTimeMillis()
    private val bufferBuilder = BufferBuilder(256)
    private val fixedBuffers = listOf(
        Sheets.shieldSheet(),
        Sheets.bedSheet(),
        Sheets.shulkerBoxSheet(),
        Sheets.signSheet(),
        Sheets.chestSheet(),
        RenderType.translucentNoCrumbling(),
        RenderType.armorGlint(),
        RenderType.armorEntityGlint(),
        RenderType.glint(),
        RenderType.glintDirect(),
        RenderType.glintTranslucent(),
        RenderType.entityGlint(),
        RenderType.entityGlintDirect(),
        RenderType.waterMask(),
    ).associateBy({ it }, { BufferBuilder(it.bufferSize()) })

    private fun tryCleanupCache() {
        if (lastEntitiesCleanupAt + 1000 >= System.currentTimeMillis()) {
            return
        }
        lastEntitiesCleanupAt = System.currentTimeMillis()
        compiledEntities.values.removeIf { entry ->
            val stale = Duration.between(entry.lastUpdate, Instant.now()) > CACHE_TIMEOUT
            if (stale) {
                entry.close()
            }
            stale
        }
    }

    @JvmStatic
    fun renderEntity(
        entity: Entity,
        cameraX: Double,
        cameraY: Double,
        cameraZ: Double,
        partialTicks: Float,
        poseStack: PoseStack,
        renderDispatcher: EntityRenderDispatcher,
        ci: CallbackInfo,
    ) {
//        if (true) {
//            return
//        }
        ci.cancel()
        tryCleanupCache()

        val cached = compiledEntities[entity.uuid]
        val xInterpolated = Mth.lerp(partialTicks.toDouble(), entity.xOld, entity.x).toFloat()
        val yInterpolated = Mth.lerp(partialTicks.toDouble(), entity.yOld, entity.y).toFloat()
        val zInterpolated = Mth.lerp(partialTicks.toDouble(), entity.zOld, entity.z).toFloat()
        val finalX = xInterpolated.toDouble() - cameraX
        val finalY = yInterpolated.toDouble() - cameraY
        val finalZ = zInterpolated.toDouble() - cameraZ

        val yRotInterpolated = Mth.lerp(partialTicks, entity.yRotO, entity.yRot)
        if (cached == null || cached.expired(entity)) {
            val bufferSource = CachedBufferSource(bufferBuilder, fixedBuffers)
            poseStack.pushPose()
            val pose = poseStack.last().pose()
            renderDispatcher.render(
                entity,
                finalX,
                finalY,
                finalZ,
                yRotInterpolated,
                partialTicks,
                poseStack,
                bufferSource,
                renderDispatcher.getPackedLightCoords(entity, partialTicks),
            )
            cached?.close()
            compiledEntities[entity.uuid] =
                CachedEntity(bufferSource.endBatch(), Matrix4f(pose), finalX, finalY, finalZ, lastUpdate = Instant.now())
            poseStack.popPose()
        } else {
            poseStack.pushPose()
            val projectionMatrix = RenderSystem.getProjectionMatrix()
            val oldMatrix = Matrix4f(cached.pose).invert()
            val newMatrix = Matrix4f(poseStack.last().pose())
            newMatrix.translate(
                (finalX - cached.finalX).toFloat(),
                (finalY - cached.finalY).toFloat(),
                (finalZ - cached.finalZ).toFloat()
            )
            val pose = newMatrix.mul(oldMatrix)
            pose.translate(0f, 0.0001f, 0f) // for some reason shadow z-fights with ground here??

            cached.buffers.forEach { (renderType, buffer) ->
                buffer.bind()
                renderType.setupRenderState()
                buffer.drawWithShader(pose, projectionMatrix, RenderSystem.getShader()!!)
                renderType.clearRenderState()
            }
            poseStack.popPose()
        }
    }
}

data class CachedEntity(
    val buffers: List<Pair<RenderType, VertexBuffer>>,
    val pose: Matrix4f,
    val finalX: Double,
    val finalY: Double,
    val finalZ: Double,
    val lastUpdate: Instant,
) : Closeable {
    fun expired(entity: Entity) = run {
        val cameraEntity = Minecraft.getInstance().gameRenderer.mainCamera.position ?: return true
        val distance = cameraEntity.distanceTo(entity.position()).toInt()
        val targetFps = when (entity) {
            is Player -> when (distance) {
                in 0..5 -> 60
                in 6..10 -> 50
                in 11..20 -> 40
                in 21..30 -> 30
                in 31..41 -> 20
                else -> 10
            }

            // TODO: optimize holograms
            // Framerate is so high near because we must update rotation of nametag.
            // If armorstand is hologram only (does nothing other than displaying floating text)
            // we could "compile" it practically once
            is ArmorStand -> {
                if (entity.hasCustomName()) {
                    when (distance) {
                        in 0..3 -> 60
                        in 4..8 -> 40
                        in 9..30 -> 20
                        else -> 10
                    }
                } else {
                    when (distance) {
                        in 0..30 -> 20
                        else -> 10
                    }
                }
            }

            is ItemFrame -> {
                when (distance) {
                    in 0..5 -> 20
                    in 6..10 -> 10
                    in 11..20 -> 5
                    else -> 1
                }
            }

            else -> when (distance) {
                in 0..3 -> 60
                in 4..10 -> 50
                in 11..20 -> 40
                in 21..30 -> 30
                in 31..41 -> 20
                else -> 10
            }
        }
        val expirationDuration = Duration.ofMillis(1000L / targetFps)
        Instant.now().isAfter(this.lastUpdate.plus(expirationDuration))
    }

    override fun close() {
        this.buffers.forEach { it.second.close() }
    }
}
