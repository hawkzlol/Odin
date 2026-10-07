package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.utils.IVec2
import net.minecraft.world.phys.AABB
import kotlin.test.Test
import kotlin.test.assertEquals

class DungeonRoomGeometryTest {
    @Test
    fun `asymmetric waypoint bounds round trip through every room rotation`() {
        val bounds = AABB(0.125, 0.25, 0.375, 0.75, 0.875, 1.25)
        for (rotation in RoomRotation.entries) {
            val room = DungeonRoom(RoomType.NORMAL, IVec2(0, 0)).apply { this.rotation = rotation }
            assertEquals(bounds, room.getRealAABB(room.getRelativeAABB(bounds)), rotation.name)
        }
    }

    @Test
    fun `east rotation turns waypoint bounds around block center`() {
        val room = DungeonRoom(RoomType.NORMAL, IVec2(0, 0)).apply { rotation = RoomRotation.EAST }
        assertEquals(
            AABB(0.375, 0.25, 0.25, 1.25, 0.875, 0.875),
            room.getRealAABB(AABB(0.125, 0.25, 0.375, 0.75, 0.875, 1.25)),
        )
    }

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
