package com.odtheking.odin.utils.network

import kotlin.test.Test
import kotlin.test.assertEquals

class WebUtilsTest {

    @Test
    fun `http failure retains upstream response body and link`() {
        val error = WebUtils.InputStreamException("rate limited", "https://example.invalid/api")

        assertEquals("rate limited : https://example.invalid/api", error.message)
    }
}
