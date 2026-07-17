package com.odtheking.odin.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortVersionTest {
    @Test
    fun `detects a newer port revision`() {
        assertTrue(isPortReleaseNewer("0.2.3+mc26.2.port.1", "0.2.3+mc26.2.port.2"))
    }

    @Test
    fun `detects a newer upstream base version`() {
        assertTrue(isPortReleaseNewer("0.2.3+mc26.2.port.9", "v0.2.4+mc26.2.port.1"))
    }

    @Test
    fun `does not report the same or an older release`() {
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.2", "0.2.3+mc26.2.port.2"))
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.2", "0.2.3+mc26.2.port.1"))
    }

    @Test
    fun `accepts an upstream base version as port revision zero`() {
        assertTrue(isPortReleaseNewer("0.2.3", "0.2.3+mc26.2.port.1"))
    }

    @Test
    fun `rejects malformed release tags without throwing`() {
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.1", "mc26.2-latest"))
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.1", "0.2.4"))
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.1", "0.2.4+mc26.3.port.1"))
        assertFalse(isPortReleaseNewer("not-a-version", "0.2.3+mc26.2.port.2"))
        assertFalse(isPortReleaseNewer("0.2.3+mc26.2.port.1", null))
    }

    @Test
    fun `routes update checks only through the public fork`() {
        assertTrue(PORT_RELEASE_PAGE.startsWith("https://github.com/hawkzlol/Odin/"))
        assertTrue(PORT_RELEASE_API.startsWith("https://api.github.com/repos/hawkzlol/Odin/"))
        assertFalse(PORT_RELEASE_PAGE.contains("odtheking"))
        assertFalse(PORT_RELEASE_API.contains("odtheking"))
    }
}
