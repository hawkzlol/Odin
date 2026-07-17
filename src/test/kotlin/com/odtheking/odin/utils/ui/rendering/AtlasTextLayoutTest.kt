package com.odtheking.odin.utils.ui.rendering

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AtlasTextLayoutTest {

    @Test
    fun coverageContrastIsClampedCenteredSymmetricAndExpandedOffThread() {
        assertEquals(0, remapGlyphCoverage(0))
        assertEquals(0, remapGlyphCoverage(29))
        assertEquals(127, remapGlyphCoverage(127))
        assertEquals(128, remapGlyphCoverage(128))
        assertEquals(255, remapGlyphCoverage(226))
        assertEquals(255, remapGlyphCoverage(255))
        for (coverage in 0..255) {
            assertEquals(255, remapGlyphCoverage(coverage) + remapGlyphCoverage(255 - coverage))
        }

        assertContentEquals(
            byteArrayOf(
                -1, -1, -1, 0,
                -1, -1, -1, 0x80.toByte(),
                -1, -1, -1, -1,
            ),
            expandGlyphCoverage(byteArrayOf(0, 0x80.toByte(), -1)),
        )
    }

    @Test
    fun measurementUsesTheExactFaceAndKerning() {
        val layout = AtlasTextLayout(
            listOf(
                face(16f, advance = 8f, kerning = mapOf(('A'.code to 'V'.code) to -2f)),
                face(18f, advance = 9f, kerning = mapOf(('A'.code to 'V'.code) to -1.5f)),
            )
        )

        assertEquals(14f, layout.width("AV", 16f), 0.001f)
        assertEquals(16.5f, layout.width("AV", 18f), 0.001f)
    }

    @Test
    fun wrappingUsesAtlasMetricsAndBreaksOverlongWords() {
        val layout = AtlasTextLayout(listOf(face(16f, advance = 5f)))

        assertEquals(listOf("Alpha", "Beta"), layout.wrap("Alpha Beta", 28f, 16f))
        assertEquals(listOf("abc", "def"), layout.wrap("abcdef", 16f, 16f))
    }

    @Test
    fun verticesApplyNanoVGTopCalibrationAndPreserveLineAdvanceAndColor() {
        val layout = AtlasTextLayout(listOf(face(16f, advance = 8f, ascent = 12f)))
        val color = 0x7FAABBCC

        val vertices = layout.vertices("A\nA", 10f, 20f, 16f, color)

        assertEquals(8, vertices.size)
        assertEquals(10f, vertices[0].x, 0.001f)
        assertEquals(19f, vertices[0].y, 0.001f)
        assertEquals(35f, vertices[4].y, 0.001f)
        assertEquals(16f, vertices[4].y - vertices[0].y, 0.001f)
        assertEquals(color, vertices[0].color)
    }

    @Test
    fun printableLatinUsesTheAtlasAndUnsupportedUnicodeUsesFallback() {
        val layout = AtlasTextLayout(listOf(face(16f, advance = 8f)))

        assertTrue(layout.supports("Click GUI"))
        assertTrue(layout.supports("line one\nline two"))
        assertTrue(layout.supports("Caf\u00e9"))
        assertFalse(layout.supports("Hello \ud83c\udf0d"))
    }

    @Test
    fun completeRunChoosesOneBackendForEveryPrefix() {
        val layout = AtlasTextLayout(
            listOf(face(16f, advance = 8f, kerning = mapOf(('A'.code to 'V'.code) to -2f)))
        )
        val fallback: (String) -> Float = { it.length * 100f }

        val atlasRun = createRunWidthMeasurer("AV", 16f, layout, fallback)
        val fallbackRun = createRunWidthMeasurer("AV\ud83c\udf0d", 16f, layout, fallback)

        assertEquals(14f, atlasRun("AV"), 0.001f)
        assertEquals(200f, fallbackRun("AV"), 0.001f)
    }

    @Test
    fun snapshotReadsNeverAdvanceTheUploadGeneration() {
        val publication = AtlasPublication<String>()
        publication.publish("atlas")
        val generationAfterUpload = publication.generation

        repeat(10_000) { assertSame("atlas", publication.snapshot()) }

        assertEquals(1, generationAfterUpload)
        assertEquals(generationAfterUpload, publication.generation)
    }

    private fun face(
        size: Float,
        advance: Float,
        ascent: Float = 12f,
        kerning: Map<Pair<Int, Int>, Float> = emptyMap(),
    ): AtlasFace {
        val first = 0x20
        val count = 0x100 - first
        val glyphs = arrayOfNulls<AtlasGlyph>(count)
        for (offset in glyphs.indices) {
            glyphs[offset] = AtlasGlyph(
                u0 = 0f,
                v0 = 0f,
                u1 = 0.5f,
                v1 = 0.5f,
                x0 = 0f,
                y0 = -10f,
                x1 = advance,
                y1 = 2f,
                advance = advance,
            )
        }
        val kerningValues = FloatArray(count * count)
        for ((pair, value) in kerning) {
            kerningValues[(pair.first - first) * count + pair.second - first] = value
        }
        return AtlasFace(size, ascent, -4f, 0f, first, glyphs, kerningValues)
    }
}
