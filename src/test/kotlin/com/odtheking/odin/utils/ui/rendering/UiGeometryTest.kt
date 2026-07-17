package com.odtheking.odin.utils.ui.rendering

import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Matrix3x2f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiGeometryTest {

    @Test
    fun explicitScissorsAreClippedToTheGuiViewport() {
        val viewport = ScreenRectangle(0, 0, 320, 180)

        val belowViewport = viewportClippedScissor(
            null,
            ScreenRectangle(20, 180, 120, 24),
            viewport,
        )
        assertEquals(0, belowViewport.width())
        assertEquals(0, belowViewport.height())

        val partiallyVisible = viewportClippedScissor(
            null,
            ScreenRectangle(20, 170, 120, 24),
            viewport,
        )
        assertEquals(ScreenRectangle(20, 170, 120, 10), partiallyVisible)

        val nested = viewportClippedScissor(
            ScreenRectangle(40, 160, 40, 20),
            ScreenRectangle(20, 150, 120, 40),
            viewport,
        )
        assertEquals(ScreenRectangle(40, 160, 40, 20), nested)
    }

    @Test
    fun boundsCoverFractionalAndNegativeEdgesExactly() {
        val vertices = listOf(
            UiVertex(-0.2f, -1.7f, -1),
            UiVertex(10.2f, 4.1f, -1),
        )

        val bounds = uiBounds(vertices, Matrix3x2f(), null)

        requireNotNull(bounds)
        assertEquals(-1, bounds.left())
        assertEquals(-2, bounds.top())
        assertEquals(11, bounds.right())
        assertEquals(5, bounds.bottom())
        assertEquals(12, bounds.width())
        assertEquals(7, bounds.height())

        val scissorExtent = coveringScreenRectangle(0.25f, -3.75f, 10.25f, 2.25f)
        assertEquals(0, scissorExtent.left())
        assertEquals(-4, scissorExtent.top())
        assertEquals(11, scissorExtent.right())
        assertEquals(3, scissorExtent.bottom())
    }

    @Test
    fun plainRectangleKeepsSolidCoreAndAddsTransparentFringe() {
        val vertices = UiGeometry.roundedFill(
            2f, 3f, 12f, 13f,
            UiRadii.uniform(0f),
            0xFF112233.toInt(),
        )

        assertEquals(20, vertices.size)
        val solid = vertices.filter { it.color ushr 24 == 255 }
        val fringe = vertices.filter { it.color ushr 24 == 0 }
        assertEquals(setOf(2f, 12f), solid.take(4).map { it.x }.toSet())
        assertEquals(setOf(3f, 13f), solid.take(4).map { it.y }.toSet())
        assertTrue(fringe.isNotEmpty())
        assertTrue(fringe.all { it.x in 1f..13f && it.y in 2f..14f })
    }

    @Test
    fun roundedGeometryIsFrozenAsCompleteQuadsWithinBounds() {
        val vertices = UiGeometry.roundedFill(
            0f, 0f, 100f, 40f,
            UiRadii(40f, 20f, 30f, 10f),
            0xFFFF0000.toInt(),
            0xFF00FF00.toInt(),
            0xFF0000FF.toInt(),
            0xFFFFFFFF.toInt(),
        )

        assertTrue(vertices.size > 4)
        assertEquals(0, vertices.size % 4)
        assertTrue(vertices.all { it.x in -1f..101f && it.y in -1f..41f })
        assertTrue(vertices.filter { it.color ushr 24 > 0 }.all { it.x in 0f..100f && it.y in 0f..40f })
        assertTrue(vertices.any { it.color ushr 24 == 0 })
        val a = vertices[0]
        val b = vertices[1]
        val c = vertices[2]
        val signedArea = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
        assertTrue(signedArea < 0f, "Rounded fan vertices must match Minecraft GUI quad winding")
    }

    @Test
    fun outlineAndShadowNeverCreatePartialPrimitives() {
        val outline = UiGeometry.roundedOutline(
            0f, 0f, 80f, 30f,
            UiRadii.uniform(8f),
            2f,
            0xFFFFFFFF.toInt(),
        )
        val shadow = UiGeometry.dropShadow(10f, 10f, 80f, 30f, 8f, 2f, 8f, 1f)

        assertTrue(outline.isNotEmpty())
        assertEquals(0, outline.size % 4)
        assertTrue(outline.any { it.color ushr 24 == 0 })
        val solidOutline = outline.filter { it.color ushr 24 == 255 }
        assertTrue(solidOutline.any { it.x == 0f })
        assertTrue(solidOutline.any { it.x == 1f })
        assertTrue(shadow.isNotEmpty())
        assertEquals(0, shadow.size % 4)
        val alphas = shadow.map { it.color ushr 24 }
        assertTrue(alphas.max() <= 125)
        assertTrue(alphas.any { it > 0 })
        assertTrue(alphas.any { it == 0 })
    }

    @Test
    fun texturedGeometryDoesNotAddAFringeOrOutOfRangeUvs() {
        val vertices = UiGeometry.roundedFill(
            2f, 3f, 12f, 13f,
            UiRadii.uniform(0f),
            0xFFFFFFFF.toInt(),
            textured = true,
        )

        assertEquals(4, vertices.size)
        assertTrue(vertices.all { it.x in 2f..12f && it.y in 3f..13f })
        assertTrue(vertices.all { it.u in 0f..1f && it.v in 0f..1f })
    }

    @Test
    fun circleAndLineKeepOpaqueCoreAndFadeOutsideIt() {
        val circle = UiGeometry.circle(10f, 10f, 5f, 0xCCFFFFFF.toInt())
        val line = UiGeometry.line(0f, 0f, 10f, 0f, 2f, 0xCCFFFFFF.toInt())

        assertEquals(0, circle.size % 4)
        assertEquals(0, line.size % 4)
        assertTrue(circle.any { it.color ushr 24 == 0 })
        assertTrue(circle.any { it.color ushr 24 == 0xCC })
        assertTrue(line.any { it.color ushr 24 == 0 })
        assertTrue(line.any { it.color ushr 24 == 0xCC })
        assertTrue(line.any { it.x == -1f })
        assertTrue(line.any { it.x == 11f })
    }
}
