package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.IVec2
import kotlin.test.Test
import kotlin.test.assertEquals

class DungeonDoorTest {

    @Test
    fun `horizontal door derives tile and world coordinates`() {
        val door = DungeonDoor(IVec2(2, 3), DoorRotation.Horizontal, DoorType.Wither)

        assertEquals(20, door.originTileIndex)
        assertEquals(21, door.destinationTileIndex)
        assertEquals(-105, door.worldX)
        assertEquals(-89, door.worldZ)
    }

    @Test
    fun `vertical door derives tile and world coordinates`() {
        val door = DungeonDoor(IVec2(2, 3), DoorRotation.Vertical, DoorType.Normal)

        assertEquals(20, door.originTileIndex)
        assertEquals(26, door.destinationTileIndex)
        assertEquals(-121, door.worldX)
        assertEquals(-73, door.worldZ)
    }

    @Test
    fun `map colors preserve special door classification`() {
        assertEquals(DoorType.Wither, DoorType.fromColor(119.toByte()))
        assertEquals(DoorType.Blood, DoorType.fromColor(RoomType.BLOOD.mapColor))
        assertEquals(DoorType.Fairy, DoorType.fromColor(RoomType.FAIRY.mapColor))
        assertEquals(DoorType.Normal, DoorType.fromColor(RoomType.NORMAL.mapColor))
    }
}
