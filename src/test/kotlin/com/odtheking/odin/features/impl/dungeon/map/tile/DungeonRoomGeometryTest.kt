package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.IVec2
import kotlin.test.Test
import kotlin.test.assertEquals

class DungeonRoomGeometryTest {
    @Test
    fun `north-facing L room derives center`() {
        val room = DungeonRoom(RoomType.NORMAL, IVec2(2, 3)).apply {
            shape = RoomShape.L
            rotation = RoomRotation.NORTH
        }

        assertEquals(IVec2(58, 68), room.center)
    }

    @Test
    fun `south-facing linear room expands across x axis`() {
        val room = DungeonRoom(RoomType.NORMAL, IVec2(1, 1)).apply {
            shape = RoomShape.ThreeByOne
            rotation = RoomRotation.SOUTH
        }

        assertEquals(IVec2(48, 28), room.center)
    }
}
