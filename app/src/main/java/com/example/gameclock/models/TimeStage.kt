package com.example.gameclock.models

import kotlinx.serialization.Serializable

@Serializable
data class TimeStage(
    val moves: Int? = null,
    val timeInSeconds: Long,
    val incrementInSeconds: Long
)
