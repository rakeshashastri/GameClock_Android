
package com.example.gameclock.models

import kotlinx.serialization.Serializable
import kotlin.random.Random

private fun generateId(): String {
    return Random.nextLong().toString()
}

@Serializable
data class TimeControl(
    val id: String = generateId(),
    val name: String,
    val timeInSeconds: Long,
    val incrementInSeconds: Long,
    val stages: List<TimeStage> = emptyList(),
    val delayType: DelayType = DelayType.NONE
) {
    init {
        require(timeInSeconds > 0) { "Time must be greater than 0 seconds" }
        require(incrementInSeconds >= 0) { "Increment must be 0 or greater" }
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

    val effectiveStages: List<TimeStage>
        get() = if (stages.isNotEmpty()) stages else listOf(
            TimeStage(moves = null, timeInSeconds = timeInSeconds, incrementInSeconds = incrementInSeconds)
        )

    val effectiveDelayType: DelayType
        get() = if (delayType != DelayType.NONE) delayType
                else if (incrementInSeconds > 0) DelayType.FISCHER
                else DelayType.NONE

    val totalTimeSeconds: Long
        get() = if (stages.isNotEmpty()) stages.first().timeInSeconds else timeInSeconds

    val isMultiStage: Boolean
        get() = stages.size > 1

    companion object {

        val BULLET_PRESETS = listOf(
            TimeControl(name = "1 min", timeInSeconds = 60, incrementInSeconds = 0),
            TimeControl(name = "1 min + 1 sec", timeInSeconds = 60, incrementInSeconds = 1),
            TimeControl(name = "2 min + 1 sec", timeInSeconds = 120, incrementInSeconds = 1)
        )

        val BLITZ_PRESETS = listOf(
            TimeControl(name = "3 min", timeInSeconds = 180, incrementInSeconds = 0),
            TimeControl(name = "3 min + 2 sec", timeInSeconds = 180, incrementInSeconds = 2),
            TimeControl(name = "5 min", timeInSeconds = 300, incrementInSeconds = 0),
            TimeControl(name = "5 min + 3 sec", timeInSeconds = 300, incrementInSeconds = 3)
        )

        val RAPID_PRESETS = listOf(
            TimeControl(name = "10 min", timeInSeconds = 600, incrementInSeconds = 0),
            TimeControl(name = "10 min + 5 sec", timeInSeconds = 600, incrementInSeconds = 5),
            TimeControl(name = "15 min + 10 sec", timeInSeconds = 900, incrementInSeconds = 10)
        )

        val CLASSICAL_PRESETS = listOf(
            TimeControl(
                name = "90+30 | 30+30 (40 moves)",
                timeInSeconds = 5400,
                incrementInSeconds = 30,
                stages = listOf(
                    TimeStage(moves = 40, timeInSeconds = 5400, incrementInSeconds = 30),
                    TimeStage(moves = null, timeInSeconds = 1800, incrementInSeconds = 30)
                ),
                delayType = DelayType.FISCHER
            ),
            TimeControl(
                name = "120+30 | 30+30 (40 moves)",
                timeInSeconds = 7200,
                incrementInSeconds = 30,
                stages = listOf(
                    TimeStage(moves = 40, timeInSeconds = 7200, incrementInSeconds = 30),
                    TimeStage(moves = null, timeInSeconds = 1800, incrementInSeconds = 30)
                ),
                delayType = DelayType.FISCHER
            )
        )

        val ALL_PRESETS = BULLET_PRESETS + BLITZ_PRESETS + RAPID_PRESETS + CLASSICAL_PRESETS
    }
}
