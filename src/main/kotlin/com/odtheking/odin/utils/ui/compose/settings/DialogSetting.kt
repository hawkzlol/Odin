package com.odtheking.odin.utils.ui.compose.settings

import androidx.compose.runtime.Composable
import com.odtheking.odin.clickgui.settings.Setting

/** Controls used only by the standalone waypoint dialogs. */
abstract class DialogSetting<T>(name: String, description: String) : Setting<T>(name, description) {
    @Composable
    abstract fun Content()
}
