package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

private const val QUIZ_START_DURATION = 220
private const val QUIZ_QUESTION_DURATION = 100

internal fun quizTimerForMessage(message: String): Triple<Int, Int, Int>? =
    when (message) {
        "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know." -> Triple(QUIZ_START_DURATION, QUIZ_START_DURATION, 1)
        "[STATUE] Oruo the Omniscient: 2 questions left... Then you will have proven your worth to me!" -> Triple(QUIZ_QUESTION_DURATION, QUIZ_QUESTION_DURATION, 2)
        "[STATUE] Oruo the Omniscient: One more question!" -> Triple(QUIZ_QUESTION_DURATION, QUIZ_QUESTION_DURATION, 3)
        else -> null
    }

internal fun decrementQuizTimer(timer: Triple<Int, Int, Int>): Triple<Int, Int, Int> {
    val (tick, maxTick, stage) = timer
    if (tick <= 0) return timer
    return Triple(tick - 1, maxTick, stage)
}
