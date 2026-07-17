package com.odtheking.odin.utils.ui.rendering

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.logging.LogUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTTPackContext
import org.lwjgl.stb.STBTTPackedchar
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private const val GLYPH_COVERAGE_CONTRAST = 1.30f
private const val NANOVG_TOP_ALIGNMENT_OFFSET = -3f

/**
 * Filtered, backend-neutral Inter glyph atlas used by Odin's vector-style UI.
 *
 * Minecraft's normal [net.minecraft.client.gui.font.FontTexture] forces nearest sampling. That is
 * appropriate for the vanilla pixel font, but it visibly aliases a scaled TrueType face. This
 * atlas is rasterized once per resource reload and is sampled through Minecraft's managed linear
 * sampler. Rendering only reads an immutable published snapshot; it never uploads per frame.
 */
internal object OdinGlyphAtlas {
    private val logger = LogUtils.getLogger()
    private const val ATLAS_SIZE = 2048
    private const val FIRST_CODEPOINT = 0x20
    private const val LAST_CODEPOINT = 0xFF
    private const val GLYPH_COUNT = LAST_CODEPOINT - FIRST_CODEPOINT + 1
    private const val PACK_PADDING = 2
    private const val PACK_OVERSAMPLE = 3
    private const val NANOVG_METRIC_SCALE = 1.22f
    private val FACE_SIZES = floatArrayOf(16f, 18f, 20f, 22f)

    private val fontResource = Identifier.fromNamespaceAndPath("odin", "font/default.ttf")
    private val textureId = Identifier.fromNamespaceAndPath("odin", "dynamic/inter_glyph_atlas")
    private val reloaderId = Identifier.fromNamespaceAndPath("odin", "inter_glyph_atlas")
    private val initialized = AtomicBoolean()
    private val publication = AtlasPublication<PublishedAtlas>()

    fun initialize() {
        if (!initialized.compareAndSet(false, true)) return

        val loader = ResourceLoader.get(PackType.CLIENT_RESOURCES)
        loader.registerReloadListener(reloaderId, object : SimpleReloadListener<PreparedAtlas>() {
            override fun prepare(state: PreparableReloadListener.SharedState): PreparedAtlas =
                prepareAtlas(state.resourceManager())

            override fun apply(prepared: PreparedAtlas, state: PreparableReloadListener.SharedState) {
                applyAtlas(prepared)
            }
        })
        loader.addListenerOrdering(ResourceReloaderKeys.Client.TEXTURES, reloaderId)
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            publication.clear()
            client.textureManager.release(textureId)
        }
    }

    fun snapshot(): PublishedAtlas? = publication.snapshot()

    private fun applyAtlas(prepared: PreparedAtlas) {
        val image = NativeImage(ATLAS_SIZE, ATLAS_SIZE, false)
        image.pixelBytes.put(0, prepared.rgba)

        val texture = try {
            DynamicTexture(Supplier { "Odin Inter glyph atlas" }, image)
        } catch (throwable: Throwable) {
            image.close()
            throw throwable
        }
        val published = try {
            val textureSetup = TextureSetup.singleTexture(
                texture.textureView,
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),
            )
            PublishedAtlas(prepared.layout, textureSetup)
        } catch (throwable: Throwable) {
            texture.close()
            throw throwable
        }

        val minecraft = Minecraft.getInstance()
        minecraft.textureManager.register(textureId, texture)
        publication.publish(published)
        logger.info("Loaded Odin's filtered Inter glyph atlas ({}x{}, {} exact sizes)", ATLAS_SIZE, ATLAS_SIZE, FACE_SIZES.size)
    }

    private fun prepareAtlas(resourceManager: ResourceManager): PreparedAtlas {
        val fontData = resourceManager.open(fontResource).use(TextureUtil::readResource)
        val coverage = MemoryUtil.memCalloc(ATLAS_SIZE * ATLAS_SIZE)
        try {
            MemoryStack.stackPush().use { stack ->
                val fontInfo = STBTTFontinfo.malloc(stack)
                check(STBTruetype.stbtt_InitFont(fontInfo, fontData)) { "Unable to initialize Odin's Inter font" }

                val context = STBTTPackContext.malloc(stack)
                check(STBTruetype.stbtt_PackBegin(context, coverage, ATLAS_SIZE, ATLAS_SIZE, 0, PACK_PADDING)) {
                    "Unable to initialize Odin's glyph atlas"
                }

                val packedFaces = ArrayList<Pair<Float, STBTTPackedchar.Buffer>>(FACE_SIZES.size)
                try {
                    STBTruetype.stbtt_PackSetOversampling(context, PACK_OVERSAMPLE, PACK_OVERSAMPLE)
                    for (size in FACE_SIZES) {
                        val packed = STBTTPackedchar.malloc(GLYPH_COUNT, stack)
                        check(
                            STBTruetype.stbtt_PackFontRange(
                                context,
                                fontData,
                                0,
                                size * NANOVG_METRIC_SCALE,
                                FIRST_CODEPOINT,
                                packed,
                            )
                        ) { "Odin's $size px Inter face did not fit in the glyph atlas" }
                        packedFaces += size to packed
                    }
                } finally {
                    STBTruetype.stbtt_PackEnd(context)
                }

                val faces = packedFaces.map { (size, packed) -> buildFace(fontInfo, size, packed, stack) }
                val bytes = ByteArray(coverage.capacity())
                coverage.get(0, bytes)
                return PreparedAtlas(expandGlyphCoverage(bytes), AtlasTextLayout(faces))
            }
        } finally {
            MemoryUtil.memFree(coverage)
            MemoryUtil.memFree(fontData)
        }
    }

    private fun buildFace(
        fontInfo: STBTTFontinfo,
        size: Float,
        packed: STBTTPackedchar.Buffer,
        stack: MemoryStack,
    ): AtlasFace {
        val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, size * NANOVG_METRIC_SCALE)
        val ascentBuffer = stack.mallocInt(1)
        val descentBuffer = stack.mallocInt(1)
        val lineGapBuffer = stack.mallocInt(1)
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascentBuffer, descentBuffer, lineGapBuffer)

        val glyphs = arrayOfNulls<AtlasGlyph>(GLYPH_COUNT)
        for (offset in 0 until GLYPH_COUNT) {
            val codepoint = FIRST_CODEPOINT + offset
            if (STBTruetype.stbtt_FindGlyphIndex(fontInfo, codepoint) == 0) continue
            val glyph = packed[offset]
            glyphs[offset] = AtlasGlyph(
                u0 = glyph.x0().toInt() / ATLAS_SIZE.toFloat(),
                v0 = glyph.y0().toInt() / ATLAS_SIZE.toFloat(),
                u1 = glyph.x1().toInt() / ATLAS_SIZE.toFloat(),
                v1 = glyph.y1().toInt() / ATLAS_SIZE.toFloat(),
                x0 = glyph.xoff(),
                y0 = glyph.yoff(),
                x1 = glyph.xoff2(),
                y1 = glyph.yoff2(),
                advance = glyph.xadvance(),
            )
        }

        val kerning = FloatArray(GLYPH_COUNT * GLYPH_COUNT)
        for (leftOffset in 0 until GLYPH_COUNT) {
            if (glyphs[leftOffset] == null) continue
            val left = FIRST_CODEPOINT + leftOffset
            for (rightOffset in 0 until GLYPH_COUNT) {
                if (glyphs[rightOffset] == null) continue
                val right = FIRST_CODEPOINT + rightOffset
                kerning[leftOffset * GLYPH_COUNT + rightOffset] =
                    STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, left, right) * scale
            }
        }

        return AtlasFace(
            size = size,
            ascent = ascentBuffer[0] * scale,
            descent = descentBuffer[0] * scale,
            lineGap = lineGapBuffer[0] * scale,
            firstCodepoint = FIRST_CODEPOINT,
            glyphs = glyphs,
            kerning = kerning,
        )
    }

    internal data class PublishedAtlas(val layout: AtlasTextLayout, val textureSetup: TextureSetup)

    private data class PreparedAtlas(val rgba: ByteArray, val layout: AtlasTextLayout)
}

/** Symmetric contrast boost matching NanoVG's denser glyph coverage without changing geometry. */
internal fun remapGlyphCoverage(coverage: Int): Int {
    val normalized = coverage.coerceIn(0, 255) / 255f
    return (((normalized - 0.5f) * GLYPH_COVERAGE_CONTRAST + 0.5f).coerceIn(0f, 1f) * 255f).roundToInt()
}

/** Worker-side expansion; the reload apply phase only performs one bulk native-image copy. */
internal fun expandGlyphCoverage(coverage: ByteArray): ByteArray {
    val rgba = ByteArray(coverage.size * 4)
    var target = 0
    for (value in coverage) {
        rgba[target] = 0xFF.toByte()
        rgba[target + 1] = 0xFF.toByte()
        rgba[target + 2] = 0xFF.toByte()
        rgba[target + 3] = remapGlyphCoverage(value.toInt() and 0xFF).toByte()
        target += 4
    }
    return rgba
}

/** Selects atlas or fallback metrics once for a complete text run, then keeps all prefixes aligned. */
internal fun createRunWidthMeasurer(
    fullText: String,
    requestedSize: Float,
    atlasLayout: AtlasTextLayout?,
    fallback: (String) -> Float,
): (String) -> Float {
    val atlas = atlasLayout?.takeIf { requestedSize > 0f && it.supports(fullText) } ?: return fallback
    return { candidate -> if (candidate.isEmpty()) 0f else atlas.width(candidate, requestedSize) }
}

internal data class AtlasGlyph(
    val u0: Float,
    val v0: Float,
    val u1: Float,
    val v1: Float,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val advance: Float,
)

internal class AtlasFace(
    val size: Float,
    val ascent: Float,
    val descent: Float,
    val lineGap: Float,
    private val firstCodepoint: Int,
    private val glyphs: Array<AtlasGlyph?>,
    private val kerning: FloatArray,
) {
    private val lastCodepoint = firstCodepoint + glyphs.size - 1

    fun glyph(codepoint: Int): AtlasGlyph? =
        if (codepoint in firstCodepoint..lastCodepoint) glyphs[codepoint - firstCodepoint] else null

    fun kerning(left: Int, right: Int): Float {
        if (left !in firstCodepoint..lastCodepoint || right !in firstCodepoint..lastCodepoint) return 0f
        return kerning[(left - firstCodepoint) * glyphs.size + right - firstCodepoint]
    }
}

/** Pure layout logic shared by drawing, measurement, wrapping, and unit tests. */
internal class AtlasTextLayout(private val faces: List<AtlasFace>) {
    init {
        require(faces.isNotEmpty()) { "At least one font face is required" }
    }

    fun supports(text: String): Boolean {
        var index = 0
        while (index < text.length) {
            val codepoint = text.codePointAt(index)
            val printableLatin = codepoint in 0x20..0x7E || codepoint in 0xA0..0xFF
            if (codepoint != '\n'.code && (!printableLatin || faces.none { it.glyph(codepoint) != null })) return false
            index += Character.charCount(codepoint)
        }
        return true
    }

    fun width(text: String, requestedSize: Float): Float {
        if (text.isEmpty() || requestedSize <= 0f) return 0f
        val face = face(requestedSize)
        val scale = requestedSize / face.size
        var lineWidth = 0f
        var maxWidth = 0f
        var previous = -1
        var index = 0
        while (index < text.length) {
            val codepoint = text.codePointAt(index)
            if (codepoint == '\n'.code) {
                maxWidth = max(maxWidth, lineWidth)
                lineWidth = 0f
                previous = -1
            } else {
                val glyph = face.glyph(codepoint) ?: return 0f
                if (previous >= 0) lineWidth += face.kerning(previous, codepoint) * scale
                lineWidth += glyph.advance * scale
                previous = codepoint
            }
            index += Character.charCount(codepoint)
        }
        return max(maxWidth, lineWidth)
    }

    fun vertices(text: String, x: Float, y: Float, requestedSize: Float, color: Int): List<UiVertex> {
        if (text.isEmpty() || requestedSize <= 0f || color ushr 24 == 0) return emptyList()
        val face = face(requestedSize)
        val scale = requestedSize / face.size
        val lineAdvance = (face.ascent - face.descent + face.lineGap) * scale
        var penX = x
        // Fontstash/NanoVG's TOP alignment places Inter's visible ink about three logical pixels
        // above the raw stbtt ascent baseline used by the packed atlas.
        var baseline = y + face.ascent * scale + NANOVG_TOP_ALIGNMENT_OFFSET
        var previous = -1
        val result = ArrayList<UiVertex>(text.length * 4)
        var index = 0
        while (index < text.length) {
            val codepoint = text.codePointAt(index)
            if (codepoint == '\n'.code) {
                penX = x
                baseline += lineAdvance
                previous = -1
                index += Character.charCount(codepoint)
                continue
            }
            val glyph = face.glyph(codepoint) ?: return emptyList()
            if (previous >= 0) penX += face.kerning(previous, codepoint) * scale
            if (glyph.u1 > glyph.u0 && glyph.v1 > glyph.v0) {
                val left = penX + glyph.x0 * scale
                val top = baseline + glyph.y0 * scale
                val right = penX + glyph.x1 * scale
                val bottom = baseline + glyph.y1 * scale
                result += UiVertex(left, top, color, glyph.u0, glyph.v0)
                result += UiVertex(left, bottom, color, glyph.u0, glyph.v1)
                result += UiVertex(right, bottom, color, glyph.u1, glyph.v1)
                result += UiVertex(right, top, color, glyph.u1, glyph.v0)
            }
            penX += glyph.advance * scale
            previous = codepoint
            index += Character.charCount(codepoint)
        }
        return result
    }

    fun wrap(text: String, maxWidth: Float, requestedSize: Float): List<String> {
        if (text.isEmpty()) return emptyList()
        if (maxWidth <= 0f || requestedSize <= 0f) return listOf(text)

        val result = ArrayList<String>()
        val paragraphs = text.split('\n')
        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                result += ""
            } else {
                wrapParagraph(paragraph, maxWidth, requestedSize, result)
            }
        }
        return result
    }

    private fun wrapParagraph(text: String, maxWidth: Float, requestedSize: Float, output: MutableList<String>) {
        var start = 0
        while (start < text.length) {
            while (start < text.length && text[start].isWhitespace()) start++
            if (start >= text.length) break

            var index = start
            var lastBreak = -1
            var emitted = false
            while (index < text.length) {
                val codepoint = text.codePointAt(index)
                val next = index + Character.charCount(codepoint)
                if (Character.isWhitespace(codepoint)) lastBreak = index
                if (width(text.substring(start, next), requestedSize) > maxWidth) {
                    val end = when {
                        lastBreak > start -> lastBreak
                        index > start -> index
                        else -> next
                    }
                    output += text.substring(start, end).trimEnd()
                    start = if (lastBreak > start) lastBreak + 1 else end
                    emitted = true
                    break
                }
                index = next
            }
            if (!emitted) {
                output += text.substring(start).trimEnd()
                break
            }
        }
    }

    private fun face(requestedSize: Float): AtlasFace = faces.minBy { abs(it.size - requestedSize) }
}

/** Publication is the sole mutation point; snapshot reads used by rendering cannot upload. */
internal class AtlasPublication<T> {
    @Volatile
    private var current: T? = null

    @Volatile
    var generation: Long = 0
        private set

    @Synchronized
    fun publish(next: T) {
        current = next
        generation++
    }

    @Synchronized
    fun clear() {
        current = null
        generation++
    }

    fun snapshot(): T? = current
}
