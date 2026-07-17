package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.clickgui.Panel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpandedSettingsSurfaceTest {

    @Test
    fun surfaceTracksOnlyTheAnimatedSettingsArea() {
        assertEquals(
            ExpandedSettingsSurface(y = 72f, height = 128f, color = 0x7D000000),
            expandedSettingsSurface(moduleY = 40f, totalHeight = Panel.HEIGHT + 128f),
        )
        assertEquals(
            ExpandedSettingsSurface(y = 72f, height = 12f, color = 0x7D000000),
            expandedSettingsSurface(moduleY = 40f, totalHeight = Panel.HEIGHT + 12f),
        )
    }

    @Test
    fun collapsedOrInvalidAreaHasNoSurface() {
        assertNull(expandedSettingsSurface(moduleY = 40f, totalHeight = Panel.HEIGHT))
        assertNull(expandedSettingsSurface(moduleY = 40f, totalHeight = Panel.HEIGHT - 1f))
    }
}
