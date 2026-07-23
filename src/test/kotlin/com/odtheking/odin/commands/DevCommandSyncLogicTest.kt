package com.odtheking.odin.commands

import kotlin.test.Test
import kotlin.test.assertEquals

class DevCommandSyncLogicTest {
    @Test
    fun `explicit room coordinate is already a tile index`() {
        assertEquals(4, resolveRoomTileCoordinate(explicitTile = 4, playerBlockCoordinate = -73))
    }

    @Test
    fun `missing room coordinate is derived from the player block`() {
        assertEquals(4, resolveRoomTileCoordinate(explicitTile = null, playerBlockCoordinate = -73))
    }
}
