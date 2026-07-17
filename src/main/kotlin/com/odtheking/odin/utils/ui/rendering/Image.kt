package com.odtheking.odin.utils.ui.rendering

import net.minecraft.resources.Identifier

/** A game-managed texture identifier. Loading, caching, reload, and GPU disposal belong to Minecraft. */
data class Image(val identifier: Identifier)
