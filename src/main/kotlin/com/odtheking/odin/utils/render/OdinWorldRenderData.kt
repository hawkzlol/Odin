package com.odtheking.odin.utils.render

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.resources.Identifier
import java.util.Collections

/**
 * Immutable data extracted for one level-render frame.
 *
 * These records deliberately contain only values needed by rendering. In particular, they do
 * not retain a level, entity, module, renderer, font, or callback past extraction.
 */
internal data class OdinWorldRenderData(
    val lines: List<LineRenderData>,
    val filledBoxes: List<FilledBoxRenderData>,
    val wireBoxes: List<WireBoxRenderData>,
    val beaconBeams: List<BeaconRenderData>,
    val texts: List<TextRenderData>,
    val texturedQuads: List<TexturedQuadRenderData>,
) {
    val isEmpty: Boolean
        get() = lines.isEmpty() && filledBoxes.isEmpty() && wireBoxes.isEmpty() &&
            beaconBeams.isEmpty() && texts.isEmpty() && texturedQuads.isEmpty()

    companion object {
        val EMPTY = OdinWorldRenderData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

internal data class LineRenderData(
    val fromX: Double,
    val fromY: Double,
    val fromZ: Double,
    val toX: Double,
    val toY: Double,
    val toZ: Double,
    val color1: Int,
    val color2: Int,
    val thickness: Float,
    val depth: Boolean,
)

internal data class FilledBoxRenderData(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float,
    val depth: Boolean,
)

internal data class WireBoxRenderData(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float,
    val thickness: Float,
    val depth: Boolean,
)

internal data class BeaconRenderData(
    val x: Int,
    val y: Int,
    val z: Int,
    val color: Int,
    val isScoping: Boolean,
    val gameTime: Long,
)

internal data class TextRenderData(
    val text: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val scale: Float,
    val depth: Boolean,
    val width: Float,
)

internal data class TexturedQuadRenderData(
    val texture: Identifier,
    val bottomLeftX: Double,
    val bottomLeftY: Double,
    val bottomLeftZ: Double,
    val topLeftX: Double,
    val topLeftY: Double,
    val topLeftZ: Double,
    val topRightX: Double,
    val topRightY: Double,
    val topRightZ: Double,
    val bottomRightX: Double,
    val bottomRightY: Double,
    val bottomRightZ: Double,
    val normalX: Float,
    val normalY: Float,
    val normalZ: Float,
    val color: Int,
    val depth: Boolean,
)

internal val ODIN_WORLD_RENDER_DATA_KEY: RenderStateDataKey<OdinWorldRenderData> =
    RenderStateDataKey.create { "odin:world_render_data" }

/** Mutable only while [com.odtheking.odin.events.RenderEvent.Extract] is being dispatched. */
class RenderConsumer {
    private val lines = ArrayList<LineRenderData>()
    private val filledBoxes = ArrayList<FilledBoxRenderData>()
    private val wireBoxes = ArrayList<WireBoxRenderData>()
    private val beaconBeams = ArrayList<BeaconRenderData>()
    private val texts = ArrayList<TextRenderData>()
    private val texturedQuads = ArrayList<TexturedQuadRenderData>()
    private var frozenData: OdinWorldRenderData? = null

    internal fun addLine(data: LineRenderData) = mutate { lines.add(data) }
    internal fun addFilledBox(data: FilledBoxRenderData) = mutate { filledBoxes.add(data) }
    internal fun addWireBox(data: WireBoxRenderData) = mutate { wireBoxes.add(data) }
    internal fun addBeaconBeam(data: BeaconRenderData) = mutate { beaconBeams.add(data) }
    internal fun addText(data: TextRenderData) = mutate { texts.add(data) }
    internal fun addTexturedQuad(data: TexturedQuadRenderData) = mutate { texturedQuads.add(data) }

    internal fun freeze(): OdinWorldRenderData = frozenData ?: OdinWorldRenderData(
        lines = immutableSnapshot(lines),
        filledBoxes = immutableSnapshot(filledBoxes),
        wireBoxes = immutableSnapshot(wireBoxes),
        beaconBeams = immutableSnapshot(beaconBeams),
        texts = immutableSnapshot(texts),
        texturedQuads = immutableSnapshot(texturedQuads),
    ).also { frozenData = it }

    private inline fun mutate(action: () -> Unit) {
        check(frozenData == null) { "World render data collector is already frozen" }
        action()
    }

    private fun <T> immutableSnapshot(source: Collection<T>): List<T> =
        Collections.unmodifiableList(ArrayList(source))
}
