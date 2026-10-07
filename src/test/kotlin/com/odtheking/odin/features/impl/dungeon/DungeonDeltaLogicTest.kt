package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.utils.skyblock.dungeon.calculateDungeonBonusScore
import kotlin.test.Test
import kotlin.test.assertEquals

class DungeonDeltaLogicTest {

    @Test
    fun `dungeon bonus score includes bat and caps crypts`() {
        assertEquals(
            19,
            calculateDungeonBonusScore(
                cryptCount = 8,
                mimicKilled = true,
                princeKilled = true,
                batKilled = 1,
                paulBonus = true,
            ),
        )
    }

    @Test
    fun `bat contributes exactly one bonus point`() {
        val withoutBat = calculateDungeonBonusScore(5, true, true, 0, false)
        val withBat = calculateDungeonBonusScore(5, true, true, 1, false)

        assertEquals(withoutBat + 1, withBat)
        assertEquals(withoutBat + 3, calculateDungeonBonusScore(5, true, true, 3, false))
    }
}
