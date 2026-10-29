package com.odtheking.odin.utils.network.hypixelapi

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiEndpointTest {
    @Test
    fun `saved old default follows the upstream endpoint migration`() {
        assertEquals(DEFAULT_HYPIXEL_API_URL, resolveHypixelApiUrl("https://api.odtheking.com/hypixel/"))
        assertEquals(DEFAULT_HYPIXEL_API_URL, resolveHypixelApiUrl("https://api.odtheking.com/hypixel"))
    }

    @Test
    fun `current default and custom endpoints are preserved`() {
        assertEquals(DEFAULT_HYPIXEL_API_URL, resolveHypixelApiUrl(DEFAULT_HYPIXEL_API_URL))
        val custom = "https://example.invalid/custom/"
        assertEquals(custom, resolveHypixelApiUrl(custom))
    }
}
