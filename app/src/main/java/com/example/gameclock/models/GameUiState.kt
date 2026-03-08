package com.example.gameclock.models

import kotlinx.serialization.Serializable

@Serializable
data class GameUiState(
    val gameState: GameState = GameState.STOPPED,
    val activePlayer: Player? = null,
    val player1TimeMs: Long = 300_000L,
    val player2TimeMs: Long = 300_000L,
    val player1TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2],
    val player2TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2],
    val winner: Player? = null,
    val recentTimeControls: List<TimeControl> = emptyList(),
    val customTimeControls: List<TimeControl> = emptyList(),
    val isDifferentTimeControls: Boolean = false,
    val player1Moves: Int = 0,
    val player2Moves: Int = 0,
    val player1StageIndex: Int = 0,
    val player2StageIndex: Int = 0,
    val delayRemainingMs: Long = 0L,
    val isLowTime1: Boolean = false,
    val isLowTime2: Boolean = false,
    val lowTimeWarningEnabled: Boolean = true,
    val lowTimeThresholdMs: Long = 30_000L,
    val tapSoundEnabled: Boolean = true
) {
    // Backward-compatible accessors (convert millis to seconds for external use)
    val player1Time: Long get() = player1TimeMs / 1000L
    val player2Time: Long get() = player2TimeMs / 1000L

    fun isValid(): Boolean {
        return when {
            player1TimeMs < 0 || player2TimeMs < 0 -> false
            winner != null && gameState != GameState.GAME_OVER -> false
            activePlayer != null && gameState !in listOf(GameState.RUNNING, GameState.PAUSED) -> false
            gameState == GameState.GAME_OVER && winner == null -> false
            recentTimeControls.size > 3 -> false
            customTimeControls.size > 5 -> false
            else -> true
        }
    }

    fun getTimeControlForPlayer(player: Player): TimeControl {
        return when (player) {
            Player.PLAYER_ONE -> player1TimeControl
            Player.PLAYER_TWO -> player2TimeControl
        }
    }

    fun getTimeMsForPlayer(player: Player): Long {
        return when (player) {
            Player.PLAYER_ONE -> player1TimeMs
            Player.PLAYER_TWO -> player2TimeMs
        }
    }

    fun getTimeForPlayer(player: Player): Long {
        return when (player) {
            Player.PLAYER_ONE -> player1Time
            Player.PLAYER_TWO -> player2Time
        }
    }

    fun getMovesForPlayer(player: Player): Int {
        return when (player) {
            Player.PLAYER_ONE -> player1Moves
            Player.PLAYER_TWO -> player2Moves
        }
    }

    fun getStageIndexForPlayer(player: Player): Int {
        return when (player) {
            Player.PLAYER_ONE -> player1StageIndex
            Player.PLAYER_TWO -> player2StageIndex
        }
    }

    fun canInteractWithTimers(): Boolean {
        return gameState in listOf(GameState.RUNNING, GameState.PAUSED) && winner == null
    }

    fun canStartGame(): Boolean {
        return gameState == GameState.STOPPED
    }

    fun canPauseGame(): Boolean {
        return gameState == GameState.RUNNING
    }

    fun canResumeGame(): Boolean {
        return gameState == GameState.PAUSED
    }

    fun canResetGame(): Boolean {
        return gameState != GameState.STOPPED
    }

    fun getOpponent(player: Player): Player {
        return when (player) {
            Player.PLAYER_ONE -> Player.PLAYER_TWO
            Player.PLAYER_TWO -> Player.PLAYER_ONE
        }
    }
}
