package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpstreamSyncRenderLogicTest {
    @Test
    fun `dev payload uses the UUID service schema without removed wing fields`() {
        val payload = JsonParser.parseString(
            buildDevPlayerBody("Example", 1.1f, 1.2f, 1.3f, "Custom", "secret"),
        ).asJsonObject

        assertEquals(setOf("devName", "size", "customName", "password"), payload.keySet())
        assertEquals("Example", payload["devName"].asString)
        assertEquals(listOf(1.1f, 1.2f, 1.3f), payload["size"].asJsonArray.map { it.asFloat })
        assertFalse(payload.has("wings"))
        assertFalse(payload.has("wingsColor"))
    }

    @Test
    fun `random player schema retains its UUID identity`() {
        val uuid = UUID.fromString("12345678-1234-5678-1234-567812345678")
        val player = PlayerSize.RandomPlayer("{}", "Example", uuid, listOf(1f, 1f, 1f))

        assertEquals(uuid, player.uuid)
    }

    @Test
    fun `profile message gate matches the upstream join trigger`() {
        assertTrue(isProfileIdMessage("Profile ID: 12345678-1234-5678-1234-567812345678"))
        assertFalse(isProfileIdMessage("Profile ID: 1234"))
        assertFalse(isProfileIdMessage("prefix Profile ID: 12345678-1234-5678-1234-567812345678"))
    }
}
