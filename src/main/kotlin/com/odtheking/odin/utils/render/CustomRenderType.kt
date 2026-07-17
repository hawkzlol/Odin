package com.odtheking.odin.utils.render

import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier
import java.util.concurrent.ConcurrentHashMap

object CustomRenderType {

    private val texturedQuadsEsp = ConcurrentHashMap<Identifier, RenderType>()

    // RenderTypes.LINES, RenderTypes.LINES_TRANSLUCENT || LINES_ESP, LINES_TRANSLUCENT_ESP

    val LINES_ESP: RenderType = RenderType.create(
        "lines-esp",
        RenderSetup.builder(CustomRenderPipelines.LINES_ESP)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    val LINES_TRANSLUCENT_ESP: RenderType = RenderType.create(
        "lines-translucent-esp",
        RenderSetup.builder(CustomRenderPipelines.LINES_TRANSLUCENT_ESP)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup()
    )

    // RenderTypes.DEBUG_FILLED_BOX || QUADS_ESP

    val QUADS_ESP: RenderType = RenderType.create(
        "quads-esp",
        RenderSetup.builder(CustomRenderPipelines.QUADS_ESP)
            .sortOnUpload()
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    fun texturedQuad(texture: Identifier, depth: Boolean): RenderType {
        if (depth) return RenderTypes.entityTranslucent(texture)

        return texturedQuadsEsp.computeIfAbsent(texture) {
            RenderType.create(
                "textured-quads-esp",
                RenderSetup.builder(CustomRenderPipelines.TEXTURED_QUADS_ESP)
                    .withTexture("Sampler0", it)
                    .useLightmap()
                    .useOverlay()
                    .sortOnUpload()
                    .createRenderSetup()
            )
        }
    }
}
