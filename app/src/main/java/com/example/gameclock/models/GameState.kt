package com.example.gameclock.models

import kotlinx.serialization.Serializable

@Serializable
enum class GameState {
    STOPPED,
    RUNNING,
    PAUSED,
    GAME_OVER
}

@Serializable
enum class Player {
    PLAYER_ONE,
    PLAYER_TWO
}