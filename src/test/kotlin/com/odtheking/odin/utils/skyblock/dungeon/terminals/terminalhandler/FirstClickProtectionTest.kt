package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirstClickProtectionTest {
    @Test
    fun `protects until but not including the configured boundary`() {
        assertTrue(isFirstClickProtected(timeOpened = 1_000, now = 1_499, protectionMs = 500))
        assertFalse(isFirstClickProtected(timeOpened = 1_000, now = 1_500, protectionMs = 500))
        assertFalse(isFirstClickProtected(timeOpened = 1_000, now = 1_501, protectionMs = 500))
    }
}
