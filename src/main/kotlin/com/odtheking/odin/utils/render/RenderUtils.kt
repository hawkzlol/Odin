package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import net.minecraft.util.StringDecomposer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/** Bridges Fabric's level extraction and submission phases without retaining mutable frame state. */
object RenderBatchManager {
    /**
     * Fabric fires END_EXTRACTION synchronously at the tail of LevelExtractor.extract. In 26.2
     * that call is made from Minecraft.renderFrame's client-thread extraction section, so legacy
     * subscribers may read client gameplay state while handling this callback. Nothing mutable
     * read there crosses the phase boundary: [RenderConsumer.freeze] copies scalar render records
     * into the LevelRenderState before this method returns.
     */
    fun extract(context: LevelExtractionContext) {
        val consumer = RenderConsumer()
        EventBus.post(RenderEvent.Extract(context, consumer))
        (context.levelState() as FabricRenderState).setData(ODIN_WORLD_RENDER_DATA_KEY, consumer.freeze())
    }

    fun submit(context: LevelRenderContext) {
        val data = (context.levelState() as FabricRenderState).getData(ODIN_WORLD_RENDER_DATA_KEY) ?: return
        if (data.isEmpty) return

        val poseStack = context.poseStack()
        val submitNodeCollector = context.submitNodeCollector()
        val camera = context.levelState().cameraRenderState
        val cameraPosition = camera.pos

        poseStack.pushPose()
        try {
            poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z)
            poseStack.submitQueuedLinesAndWireBoxes(data, submitNodeCollector)
            poseStack.submitQueuedFilledBoxes(data, submitNodeCollector)
            poseStack.submitQueuedTexturedQuads(data.texturedQuads, submitNodeCollector)
        } finally {
            poseStack.popPose()
        }

        poseStack.submitQueuedBeaconBeams(data.beaconBeams, submitNodeCollector, cameraPosition)
        poseStack.submitQueuedTexts(data.texts, submitNodeCollector, cameraPosition, camera.orientation)
    }
}

private fun Int.isFullyOpaque(): Boolean = (this ushr 24 and 0xFF) == 0xFF

private fun resolveLineRenderType(depth: Boolean, fullyOpaque: Boolean): RenderType = when {
    depth && fullyOpaque -> RenderTypes.LINES
    depth -> RenderTypes.LINES_TRANSLUCENT
    fullyOpaque -> CustomRenderType.LINES_ESP
    else -> CustomRenderType.LINES_TRANSLUCENT_ESP
}

private fun LineRenderData.renderType(): RenderType = resolveLineRenderType(
    depth = depth,
    fullyOpaque = color1.isFullyOpaque() && color2.isFullyOpaque(),
)

private fun WireBoxRenderData.renderType(): RenderType = resolveLineRenderType(
    depth = depth,
    fullyOpaque = alpha >= 0.999f,
)

private fun FilledBoxRenderData.renderType(): RenderType =
    if (depth) RenderTypes.debugFilledBox() else CustomRenderType.QUADS_ESP

private val LINE_RENDER_TYPES = listOf(
    RenderTypes.LINES,
    RenderTypes.LINES_TRANSLUCENT,
    CustomRenderType.LINES_ESP,
    CustomRenderType.LINES_TRANSLUCENT_ESP,
)

private fun PoseStack.submitQueuedLinesAndWireBoxes(
    data: OdinWorldRenderData,
    submitNodeCollector: SubmitNodeCollector,
) {
    if (data.lines.isEmpty() && data.wireBoxes.isEmpty()) return

    for (renderType in LINE_RENDER_TYPES) {
        val hasLines = data.lines.any { it.renderType() === renderType }
        val hasBoxes = data.wireBoxes.any { it.renderType() === renderType }
        if (!hasLines && !hasBoxes) continue

        submitNodeCollector.submitCustomGeometry(this, renderType) { pose, buffer ->
            for (line in data.lines) {
                if (line.renderType() !== renderType) continue
                PrimitiveRenderer.renderVector(pose, buffer, line)
            }

            for (box in data.wireBoxes) {
                if (box.renderType() !== renderType) continue
                PrimitiveRenderer.renderLineBox(pose, buffer, box)
            }
        }
    }
}

private fun PoseStack.submitQueuedFilledBoxes(
    data: OdinWorldRenderData,
    submitNodeCollector: SubmitNodeCollector,
) {
    if (data.filledBoxes.isEmpty()) return

    for (renderType in listOf(RenderTypes.debugFilledBox(), CustomRenderType.QUADS_ESP)) {
        if (data.filledBoxes.none { it.renderType() === renderType }) continue

        submitNodeCollector.submitCustomGeometry(this, renderType) { pose, buffer ->
            for (box in data.filledBoxes) {
                if (box.renderType() !== renderType) continue
                PrimitiveRenderer.addChainedFilledBoxVertices(pose, buffer, box)
            }
        }
    }
}

private data class TexturedQuadBatchKey(val texture: Identifier, val depth: Boolean)

private fun PoseStack.submitQueuedTexturedQuads(
    quads: List<TexturedQuadRenderData>,
    submitNodeCollector: SubmitNodeCollector,
) {
    if (quads.isEmpty()) return

    val batches = LinkedHashSet<TexturedQuadBatchKey>()
    quads.forEach { batches.add(TexturedQuadBatchKey(it.texture, it.depth)) }

    for (batch in batches) {
        val renderType = CustomRenderType.texturedQuad(batch.texture, batch.depth)
        submitNodeCollector.submitCustomGeometry(this, renderType) { pose, buffer ->
            for (quad in quads) {
                if (quad.texture != batch.texture || quad.depth != batch.depth) continue

                fun vertex(x: Double, y: Double, z: Double, u: Float, v: Float) {
                    buffer.addVertex(pose, x.toFloat(), y.toFloat(), z.toFloat())
                        .setColor(quad.color)
                        .setUv(u, v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(pose, quad.normalX, quad.normalY, quad.normalZ)
                }

                vertex(quad.bottomLeftX, quad.bottomLeftY, quad.bottomLeftZ, 0f, 1f)
                vertex(quad.topLeftX, quad.topLeftY, quad.topLeftZ, 0f, 0f)
                vertex(quad.topRightX, quad.topRightY, quad.topRightZ, 1f, 0f)
                vertex(quad.bottomRightX, quad.bottomRightY, quad.bottomRightZ, 1f, 1f)
            }
        }
    }
}

private fun PoseStack.submitQueuedBeaconBeams(
    beacons: List<BeaconRenderData>,
    submitNodeCollector: SubmitNodeCollector,
    camera: Vec3,
) {
    for (beacon in beacons) {
        pushPose()
        try {
            translate(beacon.x - camera.x, beacon.y - camera.y, beacon.z - camera.z)

            val centerX = beacon.x + 0.5
            val centerZ = beacon.z + 0.5
            val dx = camera.x - centerX
            val dz = camera.z - centerZ
            val distance = sqrt(dx * dx + dz * dz).toFloat()
            val radiusScale = if (beacon.isScoping) 1f else maxOf(1f, distance / 96f)

            BeaconRenderer.submitBeaconBeam(
                this,
                submitNodeCollector,
                BeaconRenderer.BEAM_LOCATION,
                1f,
                beacon.gameTime.toFloat(),
                0,
                319,
                beacon.color,
                0.2f * radiusScale,
                0.25f * radiusScale,
            )
        } finally {
            popPose()
        }
    }
}

private fun PoseStack.submitQueuedTexts(
    texts: List<TextRenderData>,
    submitNodeCollector: SubmitNodeCollector,
    cameraPosition: Vec3,
    cameraOrientation: org.joml.Quaternionfc,
) {
    val textCollector = submitNodeCollector.order(1)
    for (textData in texts) {
        pushPose()
        try {
            translate(
                textData.x - cameraPosition.x,
                textData.y - cameraPosition.y,
                textData.z - cameraPosition.z,
            )
            mulPose(cameraOrientation)
            val scaleFactor = textData.scale * 0.025f
            scale(scaleFactor, -scaleFactor, scaleFactor)

            val formattedText = FormattedCharSequence { output ->
                StringDecomposer.iterateFormatted(textData.text, Style.EMPTY, output)
            }
            textCollector.submitText(
                this,
                -textData.width / 2f,
                0f,
                formattedText,
                true,
                if (textData.depth) Font.DisplayMode.POLYGON_OFFSET else Font.DisplayMode.SEE_THROUGH,
                LightCoordsUtil.FULL_BRIGHT,
                -1,
                0,
                0,
            )
        } finally {
            popPose()
        }
    }
}

fun RenderEvent.Extract.drawTexturedQuad(
    texture: Identifier,
    pos: Vec3,
    width: Float,
    height: Float,
    yaw: Float = 0f,
    color: Color = Color(255, 255, 255),
    depth: Boolean = true,
) {
    val yawRad = Math.toRadians(yaw.toDouble())
    val rightX = cos(yawRad)
    val rightZ = sin(yawRad)
    val halfWidth = width * 0.5
    val halfHeight = height * 0.5

    consumer.addTexturedQuad(
        TexturedQuadRenderData(
            texture = texture,
            bottomLeftX = pos.x - rightX * halfWidth,
            bottomLeftY = pos.y - halfHeight,
            bottomLeftZ = pos.z - rightZ * halfWidth,
            topLeftX = pos.x - rightX * halfWidth,
            topLeftY = pos.y + halfHeight,
            topLeftZ = pos.z - rightZ * halfWidth,
            topRightX = pos.x + rightX * halfWidth,
            topRightY = pos.y + halfHeight,
            topRightZ = pos.z + rightZ * halfWidth,
            bottomRightX = pos.x + rightX * halfWidth,
            bottomRightY = pos.y - halfHeight,
            bottomRightZ = pos.z + rightZ * halfWidth,
            normalX = -rightZ.toFloat(),
            normalY = 0f,
            normalZ = rightX.toFloat(),
            color = color.rgba,
            depth = depth,
        ),
    )
}

fun RenderEvent.Extract.drawTracer(to: Vec3, color: Color, depth: Boolean, thickness: Float = 3f) {
    val camera = context.levelState().cameraRenderState
    val from = camera.pos.add(Vec3.directionFromRotation(camera.xRot, camera.yRot))
    drawLine(listOf(from, to), color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    drawLine(points, color, color, depth, thickness)
}

fun RenderEvent.Extract.drawLine(
    points: Collection<Vec3>,
    color1: Color,
    color2: Color,
    depth: Boolean,
    thickness: Float = 3f,
) {
    if (points.size < 2) return

    val color1Rgba = color1.rgba
    val color2Rgba = color2.rgba
    val iterator = points.iterator()
    var current = iterator.next()

    while (iterator.hasNext()) {
        val next = iterator.next()
        consumer.addLine(
            LineRenderData(
                current.x,
                current.y,
                current.z,
                next.x,
                next.y,
                next.z,
                color1Rgba,
                color2Rgba,
                thickness,
                depth,
            ),
        )
        current = next
    }
}

fun RenderEvent.Extract.drawWireFrameBox(
    aabb: AABB,
    color: Color,
    thickness: Float = 3f,
    depth: Boolean = false,
) {
    consumer.addWireBox(
        WireBoxRenderData(
            aabb.minX,
            aabb.minY,
            aabb.minZ,
            aabb.maxX,
            aabb.maxY,
            aabb.maxZ,
            color.redFloat,
            color.greenFloat,
            color.blueFloat,
            color.alphaFloat,
            thickness,
            depth,
        ),
    )
}

fun RenderEvent.Extract.drawFilledBox(aabb: AABB, color: Color, depth: Boolean = false) {
    consumer.addFilledBox(
        FilledBoxRenderData(
            aabb.minX,
            aabb.minY,
            aabb.minZ,
            aabb.maxX,
            aabb.maxY,
            aabb.maxZ,
            color.redFloat,
            color.greenFloat,
            color.blueFloat,
            color.alphaFloat,
            depth,
        ),
    )
}

fun RenderEvent.Extract.drawStyledBox(
    aabb: AABB,
    color: Color,
    style: Int = 0,
    depth: Boolean = true,
) {
    when (style) {
        0 -> drawFilledBox(aabb, color, depth = depth)
        1 -> drawWireFrameBox(aabb, color, depth = depth)
        2 -> {
            drawFilledBox(aabb, color.multiplyAlpha(0.5f), depth = depth)
            drawWireFrameBox(aabb, color, depth = depth)
        }
    }
}

fun RenderEvent.Extract.drawBeaconBeam(position: BlockPos, color: Color) {
    consumer.addBeaconBeam(
        BeaconRenderData(
            position.x,
            position.y,
            position.z,
            color.rgba,
            mc.player?.isScoping == true,
            context.level().gameTime,
        ),
    )
}

fun RenderEvent.Extract.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
    val font = mc.font
    val renderedText = if (font.isBidirectional) font.bidirectionalShaping(text) else text
    consumer.addText(
        TextRenderData(
            renderedText,
            pos.x,
            pos.y,
            pos.z,
            scale,
            depth,
            font.width(text).toFloat(),
        ),
    )
}

fun RenderEvent.Extract.drawCustomBeacon(
    title: String,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true,
) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = false)
    drawBeaconBeam(position, color)
    drawText(
        if (distance) "$title §r§f(§3${dist}m§f)" else title,
        Vec3.atCenterOf(position).addVec(y = 1.7),
        if (increase) max(1f, dist * 0.05f) else 2f,
        false,
    )
}

fun RenderEvent.Extract.drawCylinder(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false,
) {
    val angleStep = 2.0 * Math.PI / segments
    val rgba = color.rgba

    for (i in 0 until segments) {
        val angle1 = i * angleStep
        val angle2 = (i + 1) * angleStep

        val x1 = radius * cos(angle1)
        val z1 = radius * sin(angle1)
        val x2 = radius * cos(angle2)
        val z2 = radius * sin(angle2)

        fun addLine(from: Vec3, to: Vec3) {
            consumer.addLine(
                LineRenderData(
                    from.x,
                    from.y,
                    from.z,
                    to.x,
                    to.y,
                    to.z,
                    rgba,
                    rgba,
                    thickness,
                    depth,
                ),
            )
        }

        val top1 = center.add(x1, height.toDouble(), z1)
        val top2 = center.add(x2, height.toDouble(), z2)
        val bottom1 = center.add(x1, 0.0, z1)
        val bottom2 = center.add(x2, 0.0, z2)
        addLine(top1, top2)
        addLine(bottom1, bottom2)
        addLine(bottom1, top1)
    }
}

fun RenderEvent.Extract.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for (waypoint in waypoints) {
        val color = waypoint.color
        if (waypoint.isClicked || color.isTransparent) continue

        val aabb = waypoint.aabb.move(waypoint.blockPos)
        val depth = waypoint.depth && !disableDepth

        if (waypoint.filled) drawFilledBox(aabb, color, depth = depth)
        else drawWireFrameBox(aabb, color, depth = depth)
    }
}

object PrimitiveRenderer {
    private val edges = intArrayOf(
        0, 1, 1, 5, 5, 4, 4, 0,
        3, 2, 2, 6, 6, 7, 7, 3,
        0, 3, 1, 2, 5, 6, 4, 7,
    )

    internal fun renderLineBox(pose: PoseStack.Pose, buffer: VertexConsumer, box: WireBoxRenderData) {
        val x0 = box.minX.toFloat()
        val y0 = box.minY.toFloat()
        val z0 = box.minZ.toFloat()
        val x1 = box.maxX.toFloat()
        val y1 = box.maxY.toFloat()
        val z1 = box.maxZ.toFloat()
        val corners = floatArrayOf(
            x0, y0, z0,
            x1, y0, z0,
            x1, y1, z0,
            x0, y1, z0,
            x0, y0, z1,
            x1, y0, z1,
            x1, y1, z1,
            x0, y1, z1,
        )

        for (i in edges.indices step 2) {
            val first = edges[i] * 3
            val second = edges[i + 1] * 3
            val fromX = corners[first]
            val fromY = corners[first + 1]
            val fromZ = corners[first + 2]
            val toX = corners[second]
            val toY = corners[second + 1]
            val toZ = corners[second + 2]
            val directionX = toX - fromX
            val directionY = toY - fromY
            val directionZ = toZ - fromZ

            buffer.addVertex(pose, fromX, fromY, fromZ)
                .setColor(box.red, box.green, box.blue, box.alpha)
                .setNormal(pose, directionX, directionY, directionZ)
                .setLineWidth(box.thickness)
            buffer.addVertex(pose, toX, toY, toZ)
                .setColor(box.red, box.green, box.blue, box.alpha)
                .setNormal(pose, directionX, directionY, directionZ)
                .setLineWidth(box.thickness)
        }
    }

    internal fun addChainedFilledBoxVertices(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        box: FilledBoxRenderData,
    ) {
        val matrix = pose.pose()
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        fun vertex(x: Float, y: Float, z: Float) {
            buffer.addVertex(matrix, x, y, z).setColor(box.red, box.green, box.blue, box.alpha)
        }

        vertex(minX, minY, minZ)
        vertex(minX, minY, maxZ)
        vertex(minX, maxY, maxZ)
        vertex(minX, maxY, minZ)

        vertex(maxX, minY, maxZ)
        vertex(maxX, minY, minZ)
        vertex(maxX, maxY, minZ)
        vertex(maxX, maxY, maxZ)

        vertex(minX, minY, minZ)
        vertex(minX, maxY, minZ)
        vertex(maxX, maxY, minZ)
        vertex(maxX, minY, minZ)

        vertex(maxX, minY, maxZ)
        vertex(maxX, maxY, maxZ)
        vertex(minX, maxY, maxZ)
        vertex(minX, minY, maxZ)

        vertex(minX, minY, minZ)
        vertex(maxX, minY, minZ)
        vertex(maxX, minY, maxZ)
        vertex(minX, minY, maxZ)

        vertex(minX, maxY, maxZ)
        vertex(maxX, maxY, maxZ)
        vertex(maxX, maxY, minZ)
        vertex(minX, maxY, minZ)
    }

    internal fun renderVector(pose: PoseStack.Pose, buffer: VertexConsumer, line: LineRenderData) {
        val fromX = line.fromX.toFloat()
        val fromY = line.fromY.toFloat()
        val fromZ = line.fromZ.toFloat()
        val toX = line.toX.toFloat()
        val toY = line.toY.toFloat()
        val toZ = line.toZ.toFloat()
        val normalX = toX - fromX
        val normalY = toY - fromY
        val normalZ = toZ - fromZ

        buffer.addVertex(pose, fromX, fromY, fromZ)
            .setColor(line.color1)
            .setNormal(pose, normalX, normalY, normalZ)
            .setLineWidth(line.thickness)
        buffer.addVertex(pose, toX, toY, toZ)
            .setColor(line.color2)
            .setNormal(pose, normalX, normalY, normalZ)
            .setLineWidth(line.thickness)
    }
}
