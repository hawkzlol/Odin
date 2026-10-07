package com.odtheking.odin.clickgui.settings.impl

/** Adapts new feature declarations without changing the existing number control. */
fun <E> rangeNumberSetting(
    name: String, default: E, range: IntRange, increment: Number = 1, desc: String, unit: String = "",
): NumberSetting<E> where E : Number, E : Comparable<E> =
    NumberSetting(name, default, range.first, range.last, increment, desc, unit)

fun <E> rangeNumberSetting(
    name: String, default: E, range: ClosedFloatingPointRange<Double>, increment: Number = 1, desc: String, unit: String = "",
): NumberSetting<E> where E : Number, E : Comparable<E> =
    NumberSetting(name, default, range.start, range.endInclusive, increment, desc, unit)
