package com.odtheking.odin.events.core

import kotlin.test.Test
import kotlin.test.assertEquals

class EventBusDispatchTest {
    private class Probe : CancellableEvent()
    private class First
    private class Second

    @Test
    fun `dispatch preserves priority cancellation and subscription lifecycle`() {
        val first = First()
        val second = Second()
        val calls = mutableListOf<String>()
        try {
            EventBus.registerListener(First::class.java, Probe::class.java, 10, false) {
                calls += "first"
                it.cancel()
            }
            EventBus.registerListener(Second::class.java, Probe::class.java, 0, true) { calls += "second" }
            EventBus.registerListener(Second::class.java, Probe::class.java, -1, false) { calls += "observer" }
            EventBus.post(Probe())
            assertEquals(emptyList(), calls)
            EventBus.subscribe(second)
            EventBus.subscribe(first)
            EventBus.post(Probe())
            assertEquals(listOf("first", "observer"), calls)
            calls.clear()
            EventBus.unsubscribe(first)
            EventBus.post(Probe())
            assertEquals(listOf("second", "observer"), calls)
            calls.clear()
            EventBus.unsubscribe(second)
            EventBus.post(Probe())
            assertEquals(emptyList(), calls)
        } finally {
            EventBus.unsubscribe(first)
            EventBus.unsubscribe(second)
            EventBus.listenerArrays.remove(Probe::class.java)
            EventBus.invokers.remove(Probe::class.java)
        }
    }
}
