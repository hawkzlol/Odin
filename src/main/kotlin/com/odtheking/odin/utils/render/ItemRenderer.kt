package com.odtheking.odin.utils.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.ItemStack

/**
 * Extracts an item through Minecraft's native GUI item render-state path.
 *
 * Minecraft snapshots the model, pose, and active scissor into a native GUI item render state,
 * then renders normal items through its managed item atlas and oversized items through
 * its built-in PIP renderer. Both paths preserve the vanilla GUI lighting and alpha setup.
 */
fun GuiGraphicsExtractor.drawItemStack(item: ItemStack, x: Int, y: Int) {
    if (item.isEmpty) return
    this.item(item, x, y)
}
