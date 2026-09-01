package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> {
        val magentaPane = slots.indexOfFirst { it.item.item == Items.STAINED_GLASS_PANE.magenta() }
        val greenPane = slots.indexOfLast { it.item.item == Items.STAINED_GLASS_PANE.lime() }
        val greenClay = slots.indexOfLast { it.item.item == Items.DYED_TERRACOTTA.lime() }

        return buildList {
            add(greenPane)
            add(magentaPane)

            if (greenPane % 9 == magentaPane % 9) add(greenClay)
        }
    }

    override fun canClick(slotIndex: Int, button: Int): Boolean =
        slotIndex.equalsOneOf(16, 25, 34, 43)

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = when {
        (slotIndex / 9).equalsOneOf(0, 4) -> TerminalSolver.melodyColumColor
        (slotIndex % 9).equalsOneOf(1, 2, 3, 4) -> TerminalSolver.melodyPointerColor
        else -> TerminalSolver.melodyPointerColor
    } to null
}