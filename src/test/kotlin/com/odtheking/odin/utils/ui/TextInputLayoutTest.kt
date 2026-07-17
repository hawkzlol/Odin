package com.odtheking.odin.utils.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextInputLayoutTest {

    @Test
    fun hitTestingUsesKernedPrefixWidths() {
        val widths = mapOf("" to 0f, "A" to 10f, "AV" to 18f)
        val measure: (String) -> Float = { widths.getValue(it) }

        assertEquals(1, hitTestTextCaret("AV", 13.9f, measure))
        assertEquals(2, hitTestTextCaret("AV", 14.1f, measure))
    }

    @Test
    fun hitTestingAndNavigationNeverSplitSurrogatePairs() {
        val text = "A\ud83c\udf0dV"
        val measure: (String) -> Float = { prefix -> prefix.codePointCount(0, prefix.length) * 10f }

        assertEquals(1, hitTestTextCaret(text, 14f, measure))
        assertEquals(3, hitTestTextCaret(text, 16f, measure))
        for (x in -5..40) assertNotEquals(2, hitTestTextCaret(text, x.toFloat(), measure))

        assertEquals(3, nextCodePointBoundary(text, 1))
        assertEquals(1, previousCodePointBoundary(text, 3))
        assertEquals(3, nextCodePointBoundary(text, 2))
        assertEquals(1, previousCodePointBoundary(text, 2))
    }
}
