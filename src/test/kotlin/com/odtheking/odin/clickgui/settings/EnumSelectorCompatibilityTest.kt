package com.odtheking.odin.clickgui.settings

import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.settings.impl.EnumSelectorSetting
import com.odtheking.odin.clickgui.settings.impl.rangeNumberSetting
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumSelectorCompatibilityTest {
    private enum class Choice {
        FIRST, FILLED_OUTLINE, CUSTOM;
        override fun toString() = if (this == CUSTOM) "Terms & Levers" else name
    }

    @Test
    fun `legacy display labels load through the unchanged selector control`() {
        val setting = EnumSelectorSetting("Test", Choice.FIRST, "")
        setting.read(JsonPrimitive("Filled Outline"), Gson())
        assertEquals(Choice.FILLED_OUTLINE, setting.value)
        setting.read(JsonPrimitive("Terms & Levers"), Gson())
        assertEquals(Choice.CUSTOM, setting.value)
    }

    @Test
    fun `upstream enum names round trip and unknown values preserve the selection`() {
        val setting = EnumSelectorSetting("Test", Choice.FIRST, "")
        setting.value = Choice.FILLED_OUTLINE
        val saved = setting.write(Gson())
        assertEquals("FILLED_OUTLINE", saved.asString)
        setting.value = Choice.FIRST
        setting.read(saved, Gson())
        setting.read(JsonPrimitive("removed-option"), Gson())
        assertEquals(Choice.FILLED_OUTLINE, setting.value)
    }

    @Test
    fun `range declarations retain old numeric control bounds and step`() {
        val number = rangeNumberSetting("Size", 1f, 0.1..4.0, 0.1f, "")
        number.read(JsonPrimitive(8), Gson())
        assertEquals(4.0, number.value.toDouble())
        number.read(JsonPrimitive(1.24), Gson())
        assertEquals(1.2, number.value.toDouble(), 0.0001)
    }
}
