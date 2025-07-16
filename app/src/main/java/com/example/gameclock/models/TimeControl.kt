
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
    val incrementInSeconds: Long
) {
    init {
        require(timeInSeconds > 0) { "Time must be greater than 0 seconds" }
        require(incrementInSeconds >= 0) { "Increment must be 0 or greater" }
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

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
        
        val ALL_PRESETS = BULLET_PRESETS + BLITZ_PRESETS + RAPID_PRESETS
    }
}
