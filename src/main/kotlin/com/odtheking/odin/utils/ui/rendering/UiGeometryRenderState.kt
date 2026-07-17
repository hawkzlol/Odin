package com.odtheking.odin.utils.ui.rendering

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.util.Mth
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Immutable GUI vertex captured during render-state extraction. */
internal data class UiVertex(
    val x: Float,
    val y: Float,
    val color: Int,
    val u: Float = 0f,
    val v: Float = 0f,
)

/**
 * Backend-neutral GUI geometry. Minecraft owns the pipeline, buffers, render pass, texture views,
 * and submission lifetime; Odin only supplies immutable vertices extracted for this frame.
 */
internal class UiGeometryRenderState(
    private val renderPipeline: RenderPipeline,
    private val textures: TextureSetup,
    private val pose: Matrix3x2fc,
    private val vertices: List<UiVertex>,
    private val textured: Boolean,
    private val scissor: ScreenRectangle?,
    private val renderBounds: ScreenRectangle?,
) : GuiElementRenderState {

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        for (vertex in vertices) {
            val output = vertexConsumer.addVertexWith2DPose(pose, vertex.x, vertex.y)
            if (textured) output.setUv(vertex.u, vertex.v)
            output.setColor(vertex.color)
        }
    }

    override fun pipeline(): RenderPipeline = renderPipeline
    override fun textureSetup(): TextureSetup = textures
    override fun scissorArea(): ScreenRectangle? = scissor
    override fun bounds(): ScreenRectangle? = renderBounds
}

internal fun GuiGraphicsExtractor.submitUiGeometry(
    pose: Matrix3x2fc,
    scissor: ScreenRectangle?,
    vertices: List<UiVertex>,
    textureSetup: TextureSetup = TextureSetup.noTexture(),
    textured: Boolean = false,
) {
    if (vertices.isEmpty()) return
    if (scissor != null && (scissor.width() <= 0 || scissor.height() <= 0)) return
    val bounds = uiBounds(vertices, pose, scissor) ?: return
    guiRenderState.addGuiElement(
        UiGeometryRenderState(
            if (textured) RenderPipelines.GUI_TEXTURED else RenderPipelines.GUI,
            textureSetup,
            Matrix3x2f(pose),
            vertices.toList(),
            textured,
            scissor,
            bounds,
        )
    )
}

internal fun uiBounds(vertices: List<UiVertex>, pose: Matrix3x2fc, scissor: ScreenRectangle?): ScreenRectangle? {
    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    for (vertex in vertices) {
        minX = min(minX, vertex.x)
        minY = min(minY, vertex.y)
        maxX = max(maxX, vertex.x)
        maxY = max(maxY, vertex.y)
    }
    if (!minX.isFinite() || !minY.isFinite() || maxX <= minX || maxY <= minY) return null
    val local = coveringScreenRectangle(minX, minY, maxX, maxY)
    val transformed = local.transformMaxBounds(pose)
    return scissor?.intersection(transformed) ?: if (scissor == null) transformed else null
}

/** Smallest integer-pixel rectangle that fully covers the supplied floating-point extent. */
internal fun coveringScreenRectangle(minX: Float, minY: Float, maxX: Float, maxY: Float): ScreenRectangle {
    val left = Mth.floor(minX)
    val top = Mth.floor(minY)
    if (maxX <= minX || maxY <= minY) return ScreenRectangle(left, top, 0, 0)

    val right = Mth.ceil(maxX)
    val bottom = Mth.ceil(maxY)
    return ScreenRectangle(left, top, (right - left).coerceAtLeast(0), (bottom - top).coerceAtLeast(0))
}

internal data class UiRadii(
    val topLeft: Float,
    val topRight: Float,
    val bottomRight: Float,
    val bottomLeft: Float,
) {
    companion object {
        fun uniform(radius: Float) = UiRadii(radius, radius, radius, radius)
    }
}

internal object UiGeometry {
    private const val CORNER_SEGMENTS = 8
    private const val ANTI_ALIAS_FRINGE = 1f

    fun roundedFill(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        radii: UiRadii,
        topLeft: Int,
        topRight: Int = topLeft,
        bottomRight: Int = topLeft,
        bottomLeft: Int = topLeft,
        textured: Boolean = false,
    ): List<UiVertex> {
        if (x1 <= x0 || y1 <= y0) return emptyList()
        val normalized = normalizeRadii(x1 - x0, y1 - y0, radii)
        if (normalized == UiRadii(0f, 0f, 0f, 0f)) {
            val core = listOf(
                vertex(x0, y0, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured),
                vertex(x0, y1, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured),
                vertex(x1, y1, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured),
                vertex(x1, y0, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured),
            )
            if (textured) return core

            val inner = rectangleContour(x0, y0, x1, y1)
            val outer = rectangleContour(
                x0 - ANTI_ALIAS_FRINGE,
                y0 - ANTI_ALIAS_FRINGE,
                x1 + ANTI_ALIAS_FRINGE,
                y1 + ANTI_ALIAS_FRINGE,
            )
            return core + gradientFringe(outer, inner, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft)
        }

        val contour = shapeContour(x0, y0, x1, y1, normalized)
        val centerX = (x0 + x1) * 0.5f
        val centerY = (y0 + y1) * 0.5f
        val center = vertex(centerX, centerY, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured)
        return buildList(contour.size * 8) {
            for (index in contour.indices) {
                val current = contour[index]
                val next = contour[(index + 1) % contour.size]
                add(center)
                add(vertex(next.x, next.y, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured))
                add(vertex(current.x, current.y, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, textured))
                add(center)
            }

            if (!textured) {
                val fringeRadii = normalized.expand(ANTI_ALIAS_FRINGE)
                val outer = shapeContour(
                    x0 - ANTI_ALIAS_FRINGE,
                    y0 - ANTI_ALIAS_FRINGE,
                    x1 + ANTI_ALIAS_FRINGE,
                    y1 + ANTI_ALIAS_FRINGE,
                    fringeRadii,
                )
                addAll(gradientFringe(outer, contour, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft))
            }
        }
    }

    fun roundedOutline(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        radii: UiRadii,
        width: Float,
        color: Int,
    ): List<UiVertex> {
        val requestedWidth = width.coerceIn(0f, min(x1 - x0, y1 - y0) * 0.5f)
        if (requestedWidth <= 0f || x1 <= x0 || y1 <= y0) return emptyList()
        val clampedWidth = (requestedWidth - ANTI_ALIAS_FRINGE).coerceAtLeast(0.01f)
        val outerRadii = normalizeRadii(x1 - x0, y1 - y0, radii)
        val innerRadii = UiRadii(
            max(0f, outerRadii.topLeft - clampedWidth),
            max(0f, outerRadii.topRight - clampedWidth),
            max(0f, outerRadii.bottomRight - clampedWidth),
            max(0f, outerRadii.bottomLeft - clampedWidth),
        )
        val outer = shapeContour(x0, y0, x1, y1, outerRadii)
        val innerX0 = x0 + clampedWidth
        val innerY0 = y0 + clampedWidth
        val innerX1 = x1 - clampedWidth
        val innerY1 = y1 - clampedWidth
        if (innerX1 <= innerX0 || innerY1 <= innerY0) {
            return roundedFill(x0, y0, x1, y1, outerRadii, color)
        }
        val inner = shapeContour(innerX0, innerY0, innerX1, innerY1, innerRadii)
        val transparent = transparent(color)
        return buildList((outer.size + inner.size) * 8) {
            addAll(ring(outer, inner, color, color))

            val antiAliasedOuter = shapeContour(
                x0 - ANTI_ALIAS_FRINGE,
                y0 - ANTI_ALIAS_FRINGE,
                x1 + ANTI_ALIAS_FRINGE,
                y1 + ANTI_ALIAS_FRINGE,
                outerRadii.expand(ANTI_ALIAS_FRINGE),
            )
            addAll(ring(antiAliasedOuter, outer, transparent, color))

            if (innerX1 - innerX0 > ANTI_ALIAS_FRINGE * 2f && innerY1 - innerY0 > ANTI_ALIAS_FRINGE * 2f) {
                val antiAliasedInner = shapeContour(
                    innerX0 + ANTI_ALIAS_FRINGE,
                    innerY0 + ANTI_ALIAS_FRINGE,
                    innerX1 - ANTI_ALIAS_FRINGE,
                    innerY1 - ANTI_ALIAS_FRINGE,
                    innerRadii.contract(ANTI_ALIAS_FRINGE),
                )
                addAll(ring(inner, antiAliasedInner, color, transparent))
            }
        }
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int): List<UiVertex> {
        val dx = x2 - x1
        val dy = y2 - y1
        val length = kotlin.math.hypot(dx, dy)
        if (length <= 0f || width <= 0f) return emptyList()
        val tx = dx / length
        val ty = dy / length
        val halfWidth = width * 0.5f
        val nx = -ty * halfWidth
        val ny = tx * halfWidth
        val core = listOf(
            Point(x1 + nx, y1 + ny),
            Point(x1 - nx, y1 - ny),
            Point(x2 - nx, y2 - ny),
            Point(x2 + nx, y2 + ny),
        )
        val outerHalfWidth = halfWidth + ANTI_ALIAS_FRINGE
        val outerNx = -ty * outerHalfWidth
        val outerNy = tx * outerHalfWidth
        val capX = tx * ANTI_ALIAS_FRINGE
        val capY = ty * ANTI_ALIAS_FRINGE
        val outer = listOf(
            Point(x1 - capX + outerNx, y1 - capY + outerNy),
            Point(x1 - capX - outerNx, y1 - capY - outerNy),
            Point(x2 + capX - outerNx, y2 + capY - outerNy),
            Point(x2 + capX + outerNx, y2 + capY + outerNy),
        )
        return core.map { UiVertex(it.x, it.y, color) } + ring(outer, core, transparent(color), color)
    }

    fun circle(centerX: Float, centerY: Float, radius: Float, color: Int): List<UiVertex> {
        if (radius <= 0f) return emptyList()
        val segments = 32
        val center = UiVertex(centerX, centerY, color)
        val contour = circleContour(centerX, centerY, radius, segments)
        return buildList(segments * 8) {
            for (index in 0 until segments) {
                val current = contour[index]
                val next = contour[(index + 1) % segments]
                add(center)
                add(UiVertex(next.x, next.y, color))
                add(UiVertex(current.x, current.y, color))
                add(center)
            }
            addAll(ring(circleContour(centerX, centerY, radius + ANTI_ALIAS_FRINGE, segments), contour, transparent(color), color))
        }
    }

    fun dropShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blur: Float,
        spread: Float,
        radius: Float,
        alpha: Float,
    ): List<UiVertex> {
        if (width <= 0f || height <= 0f || alpha <= 0f) return emptyList()
        val safeSpread = spread.coerceAtLeast(0f)
        val safeBlur = blur.coerceAtLeast(0f)
        val extent = safeSpread + safeBlur
        if (extent <= 0f) return emptyList()
        val layers = max(2, min(16, Mth.ceil(extent)))
        return buildList(layers * 4 * (CORNER_SEGMENTS + 1) * 4) {
            for (layer in 0 until layers) {
                val innerExtension = extent * layer / layers
                val outerExtension = extent * (layer + 1) / layers
                val innerAlpha = shadowAlpha(innerExtension, safeSpread, safeBlur, alpha)
                val outerAlpha = shadowAlpha(outerExtension, safeSpread, safeBlur, alpha)
                val innerColor = (innerAlpha shl 24)
                val outerColor = (outerAlpha shl 24)
                val inner = contour(
                    x - innerExtension,
                    y - innerExtension,
                    x + width + innerExtension,
                    y + height + innerExtension,
                    UiRadii.uniform(radius + innerExtension),
                )
                val outer = contour(
                    x - outerExtension,
                    y - outerExtension,
                    x + width + outerExtension,
                    y + height + outerExtension,
                    UiRadii.uniform(radius + outerExtension),
                )
                addAll(ring(outer, inner, outerColor, innerColor))
            }
        }
    }

    private fun shadowAlpha(extension: Float, spread: Float, blur: Float, alpha: Float): Int {
        val falloff = if (extension <= spread || blur <= 0f) 1f else (1f - (extension - spread) / blur).coerceIn(0f, 1f).pow(2)
        return (125f * alpha.coerceIn(0f, 1f) * falloff).toInt().coerceIn(0, 255)
    }

    private fun ring(outer: List<Point>, inner: List<Point>, outerColor: Int, innerColor: Int): List<UiVertex> {
        if (outer.size != inner.size || outer.isEmpty()) return emptyList()
        return buildList(outer.size * 4) {
            for (index in outer.indices) {
                val next = (index + 1) % outer.size
                add(UiVertex(outer[index].x, outer[index].y, outerColor))
                add(UiVertex(inner[index].x, inner[index].y, innerColor))
                add(UiVertex(inner[next].x, inner[next].y, innerColor))
                add(UiVertex(outer[next].x, outer[next].y, outerColor))
            }
        }
    }

    private fun gradientFringe(
        outer: List<Point>,
        inner: List<Point>,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        topLeft: Int,
        topRight: Int,
        bottomRight: Int,
        bottomLeft: Int,
    ): List<UiVertex> {
        if (outer.size != inner.size || outer.isEmpty()) return emptyList()
        return buildList(outer.size * 4) {
            for (index in outer.indices) {
                val next = (index + 1) % outer.size
                val innerColor = vertex(
                    inner[index].x, inner[index].y, x0, y0, x1, y1,
                    topLeft, topRight, bottomRight, bottomLeft, false,
                ).color
                val nextInnerColor = vertex(
                    inner[next].x, inner[next].y, x0, y0, x1, y1,
                    topLeft, topRight, bottomRight, bottomLeft, false,
                ).color
                add(UiVertex(outer[index].x, outer[index].y, transparent(innerColor)))
                add(UiVertex(inner[index].x, inner[index].y, innerColor))
                add(UiVertex(inner[next].x, inner[next].y, nextInnerColor))
                add(UiVertex(outer[next].x, outer[next].y, transparent(nextInnerColor)))
            }
        }
    }

    private fun shapeContour(x0: Float, y0: Float, x1: Float, y1: Float, radii: UiRadii): List<Point> =
        if (radii == UiRadii(0f, 0f, 0f, 0f)) rectangleContour(x0, y0, x1, y1) else contour(x0, y0, x1, y1, radii)

    private fun rectangleContour(x0: Float, y0: Float, x1: Float, y1: Float): List<Point> = listOf(
        Point(x0, y0),
        Point(x0, y1),
        Point(x1, y1),
        Point(x1, y0),
    )

    private fun circleContour(centerX: Float, centerY: Float, radius: Float, segments: Int): List<Point> =
        List(segments) { index ->
            val angle = index * 2.0 * PI / segments
            Point(centerX + cos(angle).toFloat() * radius, centerY + sin(angle).toFloat() * radius)
        }

    private fun contour(x0: Float, y0: Float, x1: Float, y1: Float, requested: UiRadii): List<Point> {
        if (x1 <= x0 || y1 <= y0) return emptyList()
        val radii = normalizeRadii(x1 - x0, y1 - y0, requested)
        return buildList(4 * (CORNER_SEGMENTS + 1)) {
            arc(x0 + radii.topLeft, y0 + radii.topLeft, radii.topLeft, PI, PI * 1.5, this)
            arc(x1 - radii.topRight, y0 + radii.topRight, radii.topRight, PI * 1.5, PI * 2.0, this)
            arc(x1 - radii.bottomRight, y1 - radii.bottomRight, radii.bottomRight, 0.0, PI * 0.5, this)
            arc(x0 + radii.bottomLeft, y1 - radii.bottomLeft, radii.bottomLeft, PI * 0.5, PI, this)
        }
    }

    private fun arc(cx: Float, cy: Float, radius: Float, start: Double, end: Double, output: MutableList<Point>) {
        for (step in 0..CORNER_SEGMENTS) {
            val angle = start + (end - start) * step / CORNER_SEGMENTS
            output.add(Point(cx + cos(angle).toFloat() * radius, cy + sin(angle).toFloat() * radius))
        }
    }

    private fun normalizeRadii(width: Float, height: Float, radii: UiRadii): UiRadii {
        if (width <= 0f || height <= 0f) return UiRadii(0f, 0f, 0f, 0f)
        val tl = radii.topLeft.coerceAtLeast(0f)
        val tr = radii.topRight.coerceAtLeast(0f)
        val br = radii.bottomRight.coerceAtLeast(0f)
        val bl = radii.bottomLeft.coerceAtLeast(0f)
        var scale = 1f
        if (tl + tr > 0f) scale = min(scale, width / (tl + tr))
        if (bl + br > 0f) scale = min(scale, width / (bl + br))
        if (tl + bl > 0f) scale = min(scale, height / (tl + bl))
        if (tr + br > 0f) scale = min(scale, height / (tr + br))
        scale = scale.coerceIn(0f, 1f)
        return UiRadii(tl * scale, tr * scale, br * scale, bl * scale)
    }

    private fun UiRadii.expand(amount: Float) = UiRadii(
        topLeft + amount,
        topRight + amount,
        bottomRight + amount,
        bottomLeft + amount,
    )

    private fun UiRadii.contract(amount: Float) = UiRadii(
        max(0f, topLeft - amount),
        max(0f, topRight - amount),
        max(0f, bottomRight - amount),
        max(0f, bottomLeft - amount),
    )

    private fun transparent(color: Int): Int = color and 0x00FFFFFF

    private fun vertex(
        x: Float,
        y: Float,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        topLeft: Int,
        topRight: Int,
        bottomRight: Int,
        bottomLeft: Int,
        textured: Boolean,
    ): UiVertex {
        val tx = ((x - x0) / (x1 - x0)).coerceIn(0f, 1f)
        val ty = ((y - y0) / (y1 - y0)).coerceIn(0f, 1f)
        val top = interpolate(topLeft, topRight, tx)
        val bottom = interpolate(bottomLeft, bottomRight, tx)
        return UiVertex(x, y, interpolate(top, bottom, ty), if (textured) tx else 0f, if (textured) ty else 0f)
    }

    private fun interpolate(first: Int, second: Int, amount: Float): Int {
        fun channel(shift: Int): Int {
            val a = first ushr shift and 0xFF
            val b = second ushr shift and 0xFF
            return (a + (b - a) * amount).toInt().coerceIn(0, 255)
        }
        return (channel(24) shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    private data class Point(val x: Float, val y: Float)
}
