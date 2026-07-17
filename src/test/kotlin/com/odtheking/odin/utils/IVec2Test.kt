package com.odtheking.odin.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class IVec2Test {

    @Test
    fun `component arithmetic operates independently on x and z`() {
        val left = IVec2(12, -7)
        val right = IVec2(-5, 9)

        assertEquals(IVec2(7, 2), left + right)
        assertEquals(IVec2(17, -16), left - right)
    }

    @Test
    fun `sort key follows upstream x-major ordering`() {
        assertEquals(12_993, IVec2(13, -7).sortKey)
    }

    @Test
    fun `packed coordinates preserve signed values`() {
        val vector = IVec2(Int.MIN_VALUE, Int.MAX_VALUE)

        assertEquals(Int.MIN_VALUE, vector.x)
        assertEquals(Int.MAX_VALUE, vector.z)
    }
}
