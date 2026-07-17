package com.odtheking.odin.utils.ui.rendering

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2fc
import org.joml.Matrix4f

/** Minecraft-managed glyph texture rendered with filtered sampling for Odin's vector-style UI. */
internal class SmoothGlyphRenderState(
    private val pose: Matrix3x2fc,
    private val renderable: TextRenderable,
    private val scissor: ScreenRectangle?,
    private val renderBounds: ScreenRectangle?,
) : GuiElementRenderState {

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        renderable.render(Matrix4f().mul(pose), vertexConsumer, 15728880, true)
    }

    override fun pipeline(): RenderPipeline = renderable.guiPipeline()

    override fun textureSetup(): TextureSetup = TextureSetup.singleTextureWithLightmap(
        renderable.textureView(),
        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),
    )

    override fun scissorArea(): ScreenRectangle? = scissor

    override fun bounds(): ScreenRectangle? = renderBounds
}

/** Selects the correct GUI graph node for a complete text run before its atlas glyphs are added. */
internal class SmoothTextLayerAnchor(
    private val scissor: ScreenRectangle?,
    private val renderBounds: ScreenRectangle,
) : GuiElementRenderState {
    override fun buildVertices(vertexConsumer: VertexConsumer) = Unit
    override fun pipeline(): RenderPipeline = RenderPipelines.GUI
    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()
    override fun scissorArea(): ScreenRectangle? = scissor
    override fun bounds(): ScreenRectangle = renderBounds
}
