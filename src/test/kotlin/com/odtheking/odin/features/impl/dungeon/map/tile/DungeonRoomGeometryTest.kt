package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.IVec2
import kotlin.test.Test
import kotlin.test.assertEquals

class DungeonRoomGeometryTest {
    @Test
    fun `north-facing L room derives occupied tiles and center`() {
        val room = DungeonRoom(RoomType.NORMAL, IVec2(2, 3)).apply {
            shape = RoomShape.L
            rotation = RoomRotation.NORTH
        }

        assertEquals(
            listOf(IVec2(2, 3), IVec2(3, 3), IVec2(3, 4)),
            room.occupiedTiles(),
        )
        assertEquals(IVec2(58, 68), room.center)
    }

    @Test
    fun `south-facing linear room expands across x axis`() {
        val room = DungeonRoom(RoomType.NORMAL, IVec2(1, 1)).apply {
            shape = RoomShape.ThreeByOne
            rotation = RoomRotation.SOUTH
        }

        assertEquals(
            listOf(IVec2(1, 1), IVec2(2, 1), IVec2(3, 1)),
            room.occupiedTiles(),
        )
        assertEquals(IVec2(48, 28), room.center)
    }
}
