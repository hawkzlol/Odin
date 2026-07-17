package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.utils.skyblock.dungeon.calculateDungeonBonusScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DungeonDeltaLogicTest {

    @Test
    fun `positional message radius compares squared distances`() {
        assertTrue(isWithinRadius(distanceSquared = 25.0, radius = 5.0))
        assertFalse(isWithinRadius(distanceSquared = 25.01, radius = 5.0))
    }

    @Test
    fun `dungeon bonus score includes bat and caps crypts`() {
        assertEquals(
            19,
            calculateDungeonBonusScore(
                cryptCount = 8,
                mimicKilled = true,
                princeKilled = true,
                batKilled = true,
                paulBonus = true,
            ),
        )
    }

    @Test
    fun `bat contributes exactly one bonus point`() {
        val withoutBat = calculateDungeonBonusScore(5, true, true, false, false)
        val withBat = calculateDungeonBonusScore(5, true, true, true, false)

        assertEquals(withoutBat + 1, withBat)
    }
}
