package com.odtheking.odin.features.impl.render

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.getChatBreak
import com.odtheking.odin.utils.isPortReleaseNewer
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.PORT_RELEASE_API
import com.odtheking.odin.utils.PORT_RELEASE_PAGE
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.lwjgl.glfw.GLFW
import java.net.URI
import kotlin.math.max
import kotlin.math.round

internal val PROFILE_ID_MESSAGE_REGEX = Regex("Profile ID:\\s*(.{36})")

internal fun isProfileIdMessage(message: String): Boolean = PROFILE_ID_MESSAGE_REGEX.matches(message)

@AlwaysActive
object ClickGUIModule : Module(
    name = "Click GUI",
    description = "Allows you to customize the UI.",
    key = GLFW.GLFW_KEY_RIGHT_SHIFT
) {
    val enableNotification by BooleanSetting("Chat notifications", true, desc = "Sends a message when you toggle a module with a keybind")
    val clickGUIColor by ColorSetting("Color", Color(50, 150, 220), desc = "The color of the Click GUI.")

    val roundedPanelBottom by BooleanSetting("Rounded Panel Bottoms", true, desc = "Whether to extend panels to make them rounded at the bottom.")

    val hypixelApiUrl by StringSetting("API URL", "https://api.odtheking.com/hypixel/", 128, "The Hypixel API server to connect to.").hide()
    val webSocketUrl by StringSetting("Socket URL", "wss://ws.odtheking.com/", 128, "The Websocket server to connect to.").hide()

    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") { mc.gui.setScreen(HudManager) }
    val devMessage by BooleanSetting("Developer Message", false, desc = "Sends development related messages to the chat.")
    private var firstJoin by BooleanSetting("First join", true, "").hide()

    override fun onKeybind() {
        toggle()
    }

    override fun onEnable() {
        mc.gui.setScreen(ClickGUI)
        super.onEnable()
        toggle()
    }

    val panelSetting by MapSetting("Panel Settings", mutableMapOf<String, PanelData>())
    data class PanelData(var x: Float = 10f, var y: Float = 10f, var extended: Boolean = true)

    fun resetPositions() {
        Category.categories.entries.forEachIndexed { index, (categoryName, _) ->
            val setting = panelSetting.getOrPut(categoryName) { PanelData() }
            setting.x = 10f + 260f * index
            setting.y = 10f
            setting.extended = true
        }
    }

    private var latestVersionNumber: String? = null
    private var hasSentUpdateMessage = false

    init {
        OdinMod.scope.launch {
            latestVersionNumber = checkNewerVersion(OdinMod.version.toString())
        }

        on<ChatPacketEvent> {
            if (!isProfileIdMessage(value)) return@on

            if (firstJoin) {
                firstJoin = false
                ModuleManager.saveConfigurations()
                modMessage(
                    Component.literal(getChatBreak())
                        .append(Component.literal("""
                            §7Thanks for installing §3Odin ${OdinMod.version}§7!

                            §7Use §d§l/od §r§7to access GUI settings.

                            §7This is the unofficial Minecraft 26.2 port.
                        """.trimIndent()))
                        .append(Component.literal("\n"))
                        .append(
                            Component.literal("§bReport port issues on GitHub").withStyle {
                                it.withClickEvent(ClickEvent.OpenUrl(URI("https://github.com/hawkzlol/Odin/issues")))
                                    .withHoverEvent(HoverEvent.ShowText(Component.literal("https://github.com/hawkzlol/Odin/issues")))
                            },
                        )
                        .append(Component.literal("\n"))
                        .append(Component.literal(getChatBreak())),
                    "",
                )
            }

            if (hasSentUpdateMessage || latestVersionNumber == null) return@on
            hasSentUpdateMessage = true

            modMessage(
                Component.literal(getChatBreak())
                    .append(Component.literal("§3Odin 26.2 port update available: §f$latestVersionNumber\n\n"))
                    .append(
                        Component.literal("§bGitHub release").withStyle {
                            it.withClickEvent(ClickEvent.OpenUrl(URI(PORT_RELEASE_PAGE)))
                                .withHoverEvent(HoverEvent.ShowText(Component.literal(PORT_RELEASE_PAGE)))
                        },
                    )
                    .append(Component.literal("\n\n${getChatBreak()}§r")),
                "",
            )
            alert("Odin Update Available")
        }
    }

    fun getStandardGuiScale(): Float {
        val verticalScale = (mc.window.screenHeight.toFloat() / 1080f) / NVGRenderer.devicePixelRatio()
        val horizontalScale = (mc.window.screenWidth.toFloat() / 1920f) / NVGRenderer.devicePixelRatio()
        return round(max(verticalScale, horizontalScale).coerceIn(1f, 3f) * 10f) / 10f
    }

    private suspend fun checkNewerVersion(currentVersion: String): String? {
        val newest = fetchJson<Release>(PORT_RELEASE_API).getOrElse { return null }

        return if (isPortReleaseNewer(currentVersion, newest.tagName)) newest.tagName else null
    }

    private data class Release(
        @SerializedName("tag_name")
        val tagName: String
    )
}
