package com.odtheking.odin.features.impl.render

import com.google.gson.Gson
import com.odtheking.odin.features.impl.render.waypoints.Waypoint
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WaypointGeometryTest {
    @Test
    fun `area identifiers are independent of corner order`() {
        val a = BlockPos(1, 2, 3)
        val b = BlockPos(4, 5, 6)
        assertEquals(Waypoint(blockPos = a, endPos = b).id, Waypoint(blockPos = b, endPos = a).id)
    }

    @Test
    fun `radius uses horizontal hitbox distance on the configured floor`() {
        val waypoint = Waypoint(blockPos = BlockPos.ZERO, radius = 2)
        assertTrue(waypoint.intersects(AABB(2.0, 0.0, 0.0, 2.6, 1.8, 1.0)))
        assertFalse(waypoint.intersects(AABB(3.0, 0.0, 0.0, 3.6, 1.8, 1.0)))
        assertFalse(waypoint.intersects(AABB(0.0, 1.0, 0.0, 0.6, 2.8, 1.0)))
    }

    @Test
    fun `saved waypoints reconstruct transient geometry`() {
        val gson = Gson()
        val original = Waypoint(blockPos = BlockPos(2, 3, 4), radius = 3)
        val restored = gson.fromJson(gson.toJson(original), Waypoint::class.java)
        assertEquals(original.id, restored.id)
        assertEquals(original.box, restored.box)
    }
}
