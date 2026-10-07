package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.utils.capitalizeFirst
import net.minecraft.client.input.MouseButtonEvent

val Enum<*>.label: String
    get() = toString().takeIf { it != name }
        ?: name.split('_').joinToString(" ") { it.lowercase().capitalizeFirst() }

/** Exposes upstream enum values through the existing ClickGUI selector. */
class EnumSelectorSetting<E : Enum<E>>(
    name: String,
    override val default: E,
    desc: String,
    private val options: List<E> = default.declaringJavaClass.enumConstants.asList(),
) : RenderableSetting<E>(name, desc), Saving {
    private val control = SelectorSetting(name, default.label, options.map { it.label }, desc)

    override var value: E
        get() = options[control.value]
        set(value) { control.value = options.indexOf(value).coerceAtLeast(0) }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float =
        control.render(x, y, mouseX, mouseY)

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean =
        control.mouseClicked(mouseX, mouseY, click)

    override fun getHeight(): Float = control.getHeight()
    override val isHovered: Boolean get() = control.isHovered
    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        val saved = element.asString ?: return
        value = options.firstOrNull { it.name == saved }
            ?: options.firstOrNull { it.label.equals(saved, ignoreCase = true) }
            ?: return
    }
}
