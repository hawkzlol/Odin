package com.odtheking.odin.clickgui.settings

import com.google.gson.Gson
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.utils.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorSettingLoadTest {
    @Test
    fun `loaded RGB color updates the hex editor before rendering`() {
        checkLoadedColor(false)
    }

    @Test
    fun `loaded RGBA color retains alpha in the hex editor`() {
        checkLoadedColor(true)
    }

    private fun checkLoadedColor(alpha: Boolean) {
        val gson = Gson()
        val setting = ColorSetting("Test", Color(255, 0, 0), alpha, "")
        val saved = Color(0, 255, 0, 0.5f)
        setting.read(gson.toJsonTree(saved), gson)

        val field = ColorSetting::class.java.getDeclaredField("hexString").apply { isAccessible = true }
        assertEquals(saved.rgba, setting.value.rgba)
        assertEquals(saved.hex(alpha), field.get(setting))
    }
}
