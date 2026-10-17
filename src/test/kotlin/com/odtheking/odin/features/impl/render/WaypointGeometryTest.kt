package com.odtheking.odin.features.impl.render

import com.google.gson.Gson
import com.odtheking.odin.features.impl.render.waypoints.Waypoint
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.AABB
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WaypointGeometryTest {
    @Test
    fun `area identifiers are independent of corner order`() {
        val a = Vec3(1.25, 2.5, 3.75)
        val b = Vec3(4.25, 5.5, 6.75)
        assertEquals(Waypoint(blockPos = a, endPos = b).id, Waypoint(blockPos = b, endPos = a).id)
    }

    @Test
    fun `radius uses horizontal hitbox distance on the configured floor`() {
        val waypoint = Waypoint(blockPos = Vec3.ZERO, radius = 2f)
        assertTrue(waypoint.intersects(AABB(2.0, 0.0, 0.0, 2.6, 1.8, 1.0)))
        assertFalse(waypoint.intersects(AABB(3.0, 0.0, 0.0, 3.6, 1.8, 1.0)))
        assertFalse(waypoint.intersects(AABB(0.0, 1.0, 0.0, 0.6, 2.8, 1.0)))
    }

    @Test
    fun `saved waypoints reconstruct transient geometry`() {
        val gson = Gson()
        val original = Waypoint(blockPos = Vec3(2.25, 3.5, 4.75), radius = 3f)
        val restored = gson.fromJson(gson.toJson(original), Waypoint::class.java)
        assertEquals(original.id, restored.id)
        assertEquals(original.box, restored.box)
    }
}
