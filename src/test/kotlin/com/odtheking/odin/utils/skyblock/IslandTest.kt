package com.odtheking.odin.utils.skyblock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class IslandTest {
    @Test
    fun `new island names match scoreboard display names`() {
        assertEquals("Moonglade Marsh", Island.MoongladeMarsh.displayName)
        assertEquals("Torrhus Canyon", Island.TorrhusCanyon.displayName)
        assertEquals("Safari", Island.Safari.displayName)
    }

    @Test
    fun `removed Galatea identity is not retained`() {
        assertFalse(Island.entries.any { it.name == "Galatea" })
        assertFalse(Island.entries.any { it.displayName == "Galatea" })
    }
}
