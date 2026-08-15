package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuizTimerLogicTest {
    @Test
    fun `Oruo messages start each countdown stage`() {
        val first = quizTimerForMessage("[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know.")
        val second = quizTimerForMessage("[STATUE] Oruo the Omniscient: 2 questions left... Then you will have proven your worth to me!")
        val third = quizTimerForMessage("[STATUE] Oruo the Omniscient: One more question!")

        assertEquals(Triple(220, 220, 1), first)
        assertEquals(Triple(100, 100, 2), second)
        assertEquals(Triple(100, 100, 3), third)
    }

    @Test
    fun `server tick decrements only an active countdown`() {
        assertEquals(Triple(4, 10, 2), decrementQuizTimer(Triple(5, 10, 2)))
        assertEquals(Triple(0, 10, 2), decrementQuizTimer(Triple(0, 10, 2)))
    }

    @Test
    fun `unrelated chat does not replace the timer`() {
        assertNull(quizTimerForMessage("unrelated"))
    }
}
