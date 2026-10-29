package com.odtheking.odin.features.impl.render

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import kotlin.test.BeforeTest
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CustomNameReplacerTest {
    @BeforeTest
    fun bootstrap() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @AfterTest
    fun clear() = CustomNameReplacer.clear()

    @Test
    fun `whole names replace without changing longer player names or formatting codes`() {
        CustomNameReplacer.rebuild(listOf(PlayerSize.RandomPlayer("\"Alias\"", "Example", UUID.randomUUID(), listOf(1f, 1f, 1f))))
        assertEquals("§aAlias: Example2 _Example Example_", CustomNameReplacer.replaceStringIfNeeded("§aExample: Example2 _Example Example_"))
    }

    @Test
    fun `clearing and malformed entries leave input unchanged`() {
        CustomNameReplacer.rebuild(listOf(PlayerSize.RandomPlayer("{", "Example", UUID.randomUUID(), listOf(1f, 1f, 1f))))
        val original = "Example"
        assertSame(original, CustomNameReplacer.replaceStringIfNeeded(original))
        CustomNameReplacer.clear()
        assertSame(original, CustomNameReplacer.replaceStringIfNeeded(original))
    }
}
