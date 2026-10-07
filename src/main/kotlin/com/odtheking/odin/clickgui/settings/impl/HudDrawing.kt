package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor

/** Retains the existing HUD editor's physical-pixel positions and scale. */
fun GuiGraphicsExtractor.drawAtHud(hud: HudElement, content: GuiGraphicsExtractor.() -> Unit) {
    pose().pushMatrix()
    try {
        val guiScale = mc.window.guiScale
        pose().scale(1f / guiScale, 1f / guiScale)
        pose().translate(hud.x.toFloat(), hud.y.toFloat())
        pose().scale(hud.scale)
        content()
    } finally {
        pose().popMatrix()
    }
}
