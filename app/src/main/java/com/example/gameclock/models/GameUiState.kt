package com.example.gameclock.models

import kotlinx.serialization.Serializable

/**
 * Represents the complete UI state for the game clock application.
 * This is the single source of truth for all game-related state.
 */
@Serializable
data class GameUiState(
    val gameState: GameState = GameState.STOPPED,
    val activePlayer: Player? = null,
    val player1Time: Long = 300L, // 5 minutes default
    val player2Time: Long = 300L,
    val player1TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2], // 5 min default
    val player2TimeControl: TimeControl = TimeControl.BLITZ_PRESETS[2], // 5 min default
    val winner: Player? = null,
    val recentTimeControls: List<TimeControl> = emptyList(),
    val customTimeControls: List<TimeControl> = emptyList(),
    val isDifferentTimeControls: Boolean = false
) {
    /**
     * Validates the current state for consistency
     */
    fun isValid(): Boolean {
        return when {
            // Time values must be non-negative
            player1Time < 0 || player2Time < 0 -> false
            
            // Winner can only be set when game is over
            winner != null && gameState != GameState.GAME_OVER -> false
            
            // Active player can only be set when game is running or paused
            activePlayer != null && gameState !in listOf(GameState.RUNNING, GameState.PAUSED) -> false
            
            // Game over state must have a winner
            gameState == GameState.GAME_OVER && winner == null -> false
            
            // Recent time controls list should not exceed 3 items
            recentTimeControls.size > 3 -> false
            
            // Custom time controls list should not exceed 5 items
            customTimeControls.size > 5 -> false
            
            else -> true
        }
    }
    
    /**
     * Returns the time control for the specified player
     */
    fun getTimeControlForPlayer(player: Player): TimeControl {
        return when (player) {
            Player.PLAYER_ONE -> player1TimeControl
            Player.PLAYER_TWO -> player2TimeControl
        }
    }
    
    /**
     * Returns the current time for the specified player
     */
    fun getTimeForPlayer(player: Player): Long {
        return when (player) {
            Player.PLAYER_ONE -> player1Time
            Player.PLAYER_TWO -> player2Time
        }
    }
    
    /**
     * Checks if the game is in a state where timer interactions are allowed
     */
    fun canInteractWithTimers(): Boolean {
        return gameState in listOf(GameState.RUNNING, GameState.PAUSED) && winner == null
    }
    
    /**
     * Checks if the game can be started
     */
    fun canStartGame(): Boolean {
        return gameState == GameState.STOPPED
    }
    
    /**
     * Checks if the game can be paused
     */
    fun canPauseGame(): Boolean {
        return gameState == GameState.RUNNING
    }
    
    /**
     * Checks if the game can be resumed
     */
    fun canResumeGame(): Boolean {
        return gameState == GameState.PAUSED
    }
    
    /**
     * Checks if the game can be reset
     */
    fun canResetGame(): Boolean {
        return gameState != GameState.STOPPED
    }
    
    /**
     * Returns the opponent of the specified player
     */
    fun getOpponent(player: Player): Player {
        return when (player) {
            Player.PLAYER_ONE -> Player.PLAYER_TWO
            Player.PLAYER_TWO -> Player.PLAYER_ONE
        }
    }
}