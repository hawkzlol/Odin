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

    @Test
    fun `server tick protection extends the elapsed-time boundary`() {
        assertTrue(
            isFirstClickProtected(
                timeOpened = 1_000,
                now = 2_000,
                protectionMs = 500,
                useServerTicks = true,
                ticksOpened = 7,
                protectionTicks = 8,
            )
        )
        assertFalse(
            isFirstClickProtected(
                timeOpened = 1_000,
                now = 2_000,
                protectionMs = 500,
                useServerTicks = true,
                ticksOpened = 8,
                protectionTicks = 8,
            )
        )
    }

    @Test
    fun `server tick protection is disabled in singleplayer`() {
        assertFalse(
            isFirstClickProtected(
                timeOpened = 1_000,
                now = 2_000,
                protectionMs = 500,
                useServerTicks = true,
                isSingleplayer = true,
                ticksOpened = 0,
                protectionTicks = 8,
            )
        )
    }

    @Test
    fun `term simulator override disables every protection mode`() {
        assertFalse(
            isFirstClickProtected(
                timeOpened = 1_000,
                now = 1_100,
                protectionMs = 500,
                disableForTermSim = true,
                isTermSim = true,
                useServerTicks = true,
                ticksOpened = 0,
                protectionTicks = 8,
            )
        )
    }
}
