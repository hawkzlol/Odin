package com.odtheking.odin.utils.ui.rendering

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.Font as MinecraftFont
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix3x2f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Backend-neutral replacement for Odin's former NanoVG/OpenGL renderer.
 *
 * Callers still use the established immediate-looking API, but every call now executes during
 * [GuiGraphicsExtractor] extraction and appends an immutable Minecraft GUI render state. No UI
 * logic or lambda is deferred to the GPU drawing phase.
 */
object NVGRenderer {
    private val defaultFontId = Identifier.fromNamespaceAndPath("odin", "default")
    private val defaultFontRaster = FontRaster(defaultFontId, 20f)

    val defaultFont = Font("Default", defaultFontId)

    private val activeFrame = ThreadLocal<Frame?>()
    private val images = HashMap<String, Image>()

    /** Logical-to-framebuffer scale used by Odin's existing 1080p ClickGUI layout normalization. */
    fun devicePixelRatio(): Float =
        if (mc.window.screenWidth == 0) 1f else mc.window.width.toFloat() / mc.window.screenWidth.toFloat()

    /** Execute and freeze one UI frame during screen render-state extraction. */
    inline fun record(context: GuiGraphicsExtractor, content: () -> Unit) {
        beginFrame(context)
        var contentFailure: Throwable? = null
        try {
            content()
        } catch (throwable: Throwable) {
            contentFailure = throwable
            throw throwable
        } finally {
            try {
                endFrame()
            } catch (balanceFailure: Throwable) {
                val originalFailure = contentFailure
                if (originalFailure != null) {
                    originalFailure.addSuppressed(balanceFailure)
                } else {
                    throw balanceFailure
                }
            }
        }
    }

    @PublishedApi
    internal fun beginFrame(context: GuiGraphicsExtractor) {
        check(activeFrame.get() == null) { "[NVGRenderer] Nested frame recording is not supported" }
        val window = mc.window
        val scaleX = if (window.screenWidth == 0) 1f else window.width.toFloat() / window.screenWidth / window.guiScale
        val scaleY = if (window.screenHeight == 0) 1f else window.height.toFloat() / window.screenHeight / window.guiScale
        val layoutPose = Matrix3x2f(context.pose()).scale(scaleX, scaleY)
        val viewport = ScreenRectangle(0, 0, window.guiScaledWidth, window.guiScaledHeight)
        val initialScissor = context.scissorStack.peek()?.let { viewportClippedScissor(null, it, viewport) }
        activeFrame.set(Frame(context, layoutPose, viewport, initialScissor))
    }

    @PublishedApi
    internal fun endFrame() {
        val frame = frame()
        try {
            check(frame.savedStates.isEmpty()) { "[NVGRenderer] Unbalanced push/pop while recording GUI state" }
            check(frame.scissorStack.size == 1) { "[NVGRenderer] Unbalanced scissor stack while recording GUI state" }
        } finally {
            activeFrame.remove()
        }
    }

    fun push() {
        val frame = frame()
        frame.savedStates.addLast(SavedState(Matrix3x2f(frame.pose), frame.alpha))
    }

    fun pop() {
        val frame = frame()
        val saved = frame.savedStates.removeLastOrNull()
            ?: throw IllegalStateException("[NVGRenderer] Transform stack underflow")
        frame.pose.set(saved.pose)
        frame.alpha = saved.alpha
    }

    fun scale(x: Float, y: Float) {
        frame().pose.scale(x, y)
    }

    fun translate(x: Float, y: Float) {
        frame().pose.translate(x, y)
    }

    fun rotate(amount: Float) {
        frame().pose.rotate(amount)
    }

    fun globalAlpha(amount: Float) {
        frame().alpha = amount.coerceIn(0f, 1f)
    }

    /** Start an explicit GUI stratum to isolate independently layered command groups. */
    fun nextLayer() {
        frame().context.guiRenderState.nextStratum()
    }

    fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        val frame = frame()
        val local = coveringScreenRectangle(x, y, x + w.coerceAtLeast(0f), y + h.coerceAtLeast(0f))
        val transformed = local.transformMaxBounds(frame.pose)
        val previous = frame.scissorStack.last()
        frame.scissorStack.add(viewportClippedScissor(previous, transformed, frame.viewport))
    }

    fun popScissor() {
        val frame = frame()
        check(frame.scissorStack.size > 1) { "[NVGRenderer] Scissor stack underflow" }
        frame.scissorStack.removeLast()
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
        submit(UiGeometry.line(x1, y1, x2, y2, thickness, withFrameAlpha(color)))
    }

    fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean) {
        val radii = if (roundTop) UiRadii(radius, radius, 0f, 0f) else UiRadii(0f, 0f, radius, radius)
        submit(UiGeometry.roundedFill(x, y, x + w, y + h, radii, withFrameAlpha(color)))
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float) {
        submit(UiGeometry.roundedFill(x, y, x + w, y + h + 0.5f, UiRadii.uniform(radius), withFrameAlpha(color)))
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        submit(UiGeometry.roundedFill(x, y, x + w, y + h + 0.5f, UiRadii.uniform(0f), withFrameAlpha(color)))
    }

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
        submit(UiGeometry.roundedOutline(x, y, x + w, y + h, UiRadii.uniform(radius), thickness, withFrameAlpha(color)))
    }

    fun gradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color1: Int,
        color2: Int,
        gradient: Gradient,
        radius: Float,
    ) {
        val first = withFrameAlpha(color1)
        val second = withFrameAlpha(color2)
        val colors = when (gradient) {
            Gradient.LeftToRight -> intArrayOf(first, second, second, first)
            Gradient.TopToBottom -> intArrayOf(first, first, second, second)
        }
        submit(UiGeometry.roundedFill(x, y, x + w, y + h, UiRadii.uniform(radius), colors[0], colors[1], colors[2], colors[3]))
    }

    fun dropShadow(x: Float, y: Float, width: Float, height: Float, blur: Float, spread: Float, radius: Float) {
        submit(UiGeometry.dropShadow(x, y, width, height, blur, spread, radius, frame().alpha))
    }

    fun circle(x: Float, y: Float, radius: Float, color: Int) {
        submit(UiGeometry.circle(x, y, radius, withFrameAlpha(color)))
    }

    fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        val drawColor = withFrameAlpha(color)
        val atlas = atlasFor(text, font)
        if (atlas != null) {
            submit(atlas.layout.vertices(text, x, y, size, drawColor), atlas.textureSetup, true)
            return
        }
        val raster = fontRaster(font)
        submitMinecraftText(styled(text, raster.identifier).visualOrderText, x, y + 0.5f, size / raster.size, drawColor, false)
    }

    fun textShadow(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        val drawColor = withFrameAlpha(color)
        val shadowColor = withFrameAlpha(0xFF000000.toInt())
        val shadowX = round(x + 2f)
        val shadowY = round(y + 2f)
        val foregroundX = round(x)
        val foregroundY = round(y)
        val atlas = atlasFor(text, font)
        if (atlas != null) {
            val shadow = atlas.layout.vertices(text, shadowX, shadowY, size, shadowColor)
            val foreground = atlas.layout.vertices(text, foregroundX, foregroundY, size, drawColor)
            submit(shadow + foreground, atlas.textureSetup, true)
            return
        }
        val raster = fontRaster(font)
        val sequence = styled(text, raster.identifier).visualOrderText
        val scale = size / raster.size
        submitMinecraftText(sequence, shadowX, shadowY, scale, shadowColor, false)
        submitMinecraftText(sequence, foregroundX, foregroundY, scale, drawColor, false)
    }

    fun textWidth(text: String, size: Float, font: Font): Float {
        if (text.isEmpty() || size <= 0f) return 0f
        val atlas = atlasFor(text, font)
        if (atlas != null) return atlas.layout.width(text, size)
        val raster = fontRaster(font)
        return mc.font.width(styled(text, raster.identifier)) * (size / raster.size)
    }

    /**
     * Captures one metrics backend for an entire editable run. Prefixes must not independently
     * switch to the Inter atlas when the complete run is rendered through Minecraft's fallback.
     */
    internal fun textMeasurer(fullText: String, size: Float, font: Font): (String) -> Float {
        if (size <= 0f) return { 0f }
        val atlasLayout = if (font.identifier == defaultFontId) OdinGlyphAtlas.snapshot()?.layout else null
        val raster = fontRaster(font)
        val scale = size / raster.size
        return createRunWidthMeasurer(fullText, size, atlasLayout) { candidate ->
            if (candidate.isEmpty()) 0f else mc.font.width(styled(candidate, raster.identifier)) * scale
        }
    }

    fun drawWrappedString(
        text: String,
        x: Float,
        y: Float,
        w: Float,
        size: Float,
        color: Int,
        font: Font,
        lineHeight: Float = 1f,
    ) {
        if (text.isEmpty() || w <= 0f || size <= 0f) return
        val atlas = atlasFor(text, font)
        if (atlas != null) {
            val advance = size * lineHeight.coerceAtLeast(0f)
            val drawColor = withFrameAlpha(color)
            atlas.layout.wrap(text, w, size).forEachIndexed { index, line ->
                submit(
                    atlas.layout.vertices(line, x, y + index * advance, size, drawColor),
                    atlas.textureSetup,
                    true,
                )
            }
            return
        }
        val raster = fontRaster(font)
        val scale = size / raster.size
        val lines = mc.font.split(styled(text, raster.identifier), max(1, (w / scale).toInt()))
        val advance = size * lineHeight.coerceAtLeast(0f)
        val drawColor = withFrameAlpha(color)
        lines.forEachIndexed { index, line -> submitMinecraftText(line, x, y + index * advance, scale, drawColor, false) }
    }

    fun wrappedTextBounds(
        text: String,
        w: Float,
        size: Float,
        font: Font,
        lineHeight: Float = 1f,
    ): FloatArray {
        if (text.isEmpty() || w <= 0f || size <= 0f) return floatArrayOf(0f, 0f, 0f, 0f)
        val atlas = atlasFor(text, font)
        if (atlas != null) {
            val lines = atlas.layout.wrap(text, w, size)
            val width = lines.maxOfOrNull { atlas.layout.width(it, size) } ?: 0f
            return floatArrayOf(0f, 0f, min(w, width), lines.size * size * lineHeight.coerceAtLeast(0f))
        }
        val raster = fontRaster(font)
        val scale = size / raster.size
        val lines = mc.font.split(styled(text, raster.identifier), max(1, (w / scale).toInt()))
        val width = lines.maxOfOrNull { mc.font.width(it) * scale } ?: 0f
        return floatArrayOf(0f, 0f, min(w, width), lines.size * size * lineHeight.coerceAtLeast(0f))
    }

    fun image(image: Image, x: Float, y: Float, w: Float, h: Float, radius: Float) {
        val texture = mc.textureManager.getTexture(image.identifier)
        val vertices = UiGeometry.roundedFill(
            x,
            y,
            x + w,
            y + h + 0.5f,
            UiRadii.uniform(radius),
            withFrameAlpha(-1),
            textured = true,
        )
        submit(vertices, TextureSetup.singleTexture(texture.textureView, texture.sampler), true)
    }

    fun image(image: Image, x: Float, y: Float, w: Float, h: Float) = image(image, x, y, w, h, 0f)

    fun createImage(resourcePath: String): Image = images.getOrPut(resourcePath) {
        Image(resourceIdentifier(resourcePath))
    }

    fun deleteImage(image: Image) {
        // createImage only caches resource-pack descriptors; TextureManager owns their lifetime.
        images.entries.removeIf { it.value == image }
    }

    private fun submit(vertices: List<UiVertex>, textureSetup: TextureSetup = TextureSetup.noTexture(), textured: Boolean = false) {
        val frame = frame()
        frame.context.submitUiGeometry(frame.pose, frame.scissorStack.last(), vertices, textureSetup, textured)
    }

    private fun submitMinecraftText(
        text: FormattedCharSequence,
        x: Float,
        y: Float,
        scale: Float,
        color: Int,
        shadow: Boolean,
    ) {
        if (scale <= 0f || color ushr 24 == 0) return
        val frame = frame()
        val pose = Matrix3x2f(frame.pose).translate(x, y).scale(scale, scale)
        val scissor = frame.scissorStack.last()
        if (scissor != null && (scissor.width() <= 0 || scissor.height() <= 0)) return
        val prepared = mc.font.prepareText(text, 0f, 0f, color, shadow, false, 0)
        val bounds = prepared.bounds()?.transformMaxBounds(pose)?.let { transformed ->
            scissor?.intersection(transformed) ?: if (scissor == null) transformed else null
        } ?: return
        val renderables = ArrayList<TextRenderable>()
        prepared.visit(object : MinecraftFont.GlyphVisitor {
            override fun acceptRenderable(renderable: TextRenderable) {
                renderables += renderable
            }
        })
        if (renderables.isEmpty()) return
        frame.context.guiRenderState.addGuiElement(SmoothTextLayerAnchor(scissor, bounds))
        for (renderable in renderables) {
            frame.context.guiRenderState.addGlyphToCurrentLayer(SmoothGlyphRenderState(pose, renderable, scissor, null))
        }
    }

    private fun atlasFor(text: String, font: Font): OdinGlyphAtlas.PublishedAtlas? {
        if (font.identifier != defaultFontId) return null
        return OdinGlyphAtlas.snapshot()?.takeIf { it.layout.supports(text) }
    }

    private fun fontRaster(font: Font): FontRaster {
        if (font.identifier != defaultFontId) return FontRaster(font.identifier, 16f)
        return defaultFontRaster
    }

    private fun styled(text: String, identifier: Identifier): Component =
        Component.literal(text).withStyle { style -> style.withFont(FontDescription.Resource(identifier)) }

    private fun withFrameAlpha(color: Int): Int {
        val sourceAlpha = color ushr 24 and 0xFF
        val alpha = (sourceAlpha * frame().alpha).toInt().coerceIn(0, 255)
        return color and 0x00FFFFFF or (alpha shl 24)
    }

    private fun frame(): Frame = activeFrame.get()
        ?: throw IllegalStateException("[NVGRenderer] Drawing is only valid during GUI render-state extraction")

    private fun resourceIdentifier(path: String): Identifier {
        val normalized = path.trim().replace('\\', '/').removePrefix("/")
        if (normalized.startsWith("assets/")) {
            val rest = normalized.removePrefix("assets/")
            val namespaceEnd = rest.indexOf('/')
            require(namespaceEnd > 0) { "Invalid asset path: $path" }
            return Identifier.fromNamespaceAndPath(rest.substring(0, namespaceEnd), rest.substring(namespaceEnd + 1))
        }
        return Identifier.parse(normalized)
    }

    private class Frame(
        val context: GuiGraphicsExtractor,
        val pose: Matrix3x2f,
        val viewport: ScreenRectangle,
        initialScissor: ScreenRectangle?,
        var alpha: Float = 1f,
        val savedStates: ArrayDeque<SavedState> = ArrayDeque(),
        val scissorStack: MutableList<ScreenRectangle?> = mutableListOf(initialScissor),
    )

    private data class SavedState(val pose: Matrix3x2f, val alpha: Float)

    private data class FontRaster(val identifier: Identifier, val size: Float)
}

/** Intersects an explicit scissor with its parent and the current GUI viewport. */
internal fun viewportClippedScissor(
    previous: ScreenRectangle?,
    candidate: ScreenRectangle,
    viewport: ScreenRectangle,
): ScreenRectangle {
    val visibleCandidate = candidate.intersection(viewport) ?: return ScreenRectangle.empty()
    return previous?.intersection(visibleCandidate)
        ?: if (previous == null) visibleCandidate else ScreenRectangle.empty()
}
