package com.example.gameclock.models

import org.junit.Test
import org.junit.Assert.*

class GameUiStateTest {

    @Test
    fun `default state should be valid`() {
        val state = GameUiState()
        
        assertTrue("Default state should be valid", state.isValid())
        assertEquals(GameState.STOPPED, state.gameState)
        assertNull(state.activePlayer)
        assertEquals(300L, state.player1Time)
        assertEquals(300L, state.player2Time)
        assertNull(state.winner)
        assertTrue(state.recentTimeControls.isEmpty())
        assertTrue(state.customTimeControls.isEmpty())
        assertFalse(state.isDifferentTimeControls)
    }

    @Test
    fun `state with negative time should be invalid`() {
        val state = GameUiState(player1Time = -1L)
        assertFalse("State with negative player1 time should be invalid", state.isValid())
        
        val state2 = GameUiState(player2Time = -1L)
        assertFalse("State with negative player2 time should be invalid", state2.isValid())
    }

    @Test
    fun `winner without game over state should be invalid`() {
        val state = GameUiState(
            gameState = GameState.RUNNING,
            winner = Player.PLAYER_ONE
        )
        assertFalse("Winner without GAME_OVER state should be invalid", state.isValid())
    }

    @Test
    fun `game over state without winner should be invalid`() {
        val state = GameUiState(
            gameState = GameState.GAME_OVER,
            winner = null
        )
        assertFalse("GAME_OVER state without winner should be invalid", state.isValid())
    }

    @Test
    fun `active player without running or paused state should be invalid`() {
        val state = GameUiState(
            gameState = GameState.STOPPED,
            activePlayer = Player.PLAYER_ONE
        )
        assertFalse("Active player with STOPPED state should be invalid", state.isValid())
        
        val state2 = GameUiState(
            gameState = GameState.GAME_OVER,
            activePlayer = Player.PLAYER_ONE
        )
        assertFalse("Active player with GAME_OVER state should be invalid", state2.isValid())
    }

    @Test
    fun `valid running state with active player`() {
        val state = GameUiState(
            gameState = GameState.RUNNING,
            activePlayer = Player.PLAYER_ONE
        )
        assertTrue("Running state with active player should be valid", state.isValid())
    }

    @Test
    fun `valid paused state with active player`() {
        val state = GameUiState(
            gameState = GameState.PAUSED,
            activePlayer = Player.PLAYER_TWO
        )
        assertTrue("Paused state with active player should be valid", state.isValid())
    }

    @Test
    fun `valid game over state with winner`() {
        val state = GameUiState(
            gameState = GameState.GAME_OVER,
            winner = Player.PLAYER_ONE,
            activePlayer = null
        )
        assertTrue("Game over state with winner should be valid", state.isValid())
    }

    @Test
    fun `too many recent time controls should be invalid`() {
        val timeControls = List(4) { 
            TimeControl(name = "Test $it", timeInSeconds = 300, incrementInSeconds = 0)
        }
        val state = GameUiState(recentTimeControls = timeControls)
        assertFalse("More than 3 recent time controls should be invalid", state.isValid())
    }

    @Test
    fun `too many custom time controls should be invalid`() {
        val timeControls = List(6) { 
            TimeControl(name = "Custom $it", timeInSeconds = 300, incrementInSeconds = 0)
        }
        val state = GameUiState(customTimeControls = timeControls)
        assertFalse("More than 5 custom time controls should be invalid", state.isValid())
    }

    @Test
    fun `maximum allowed time controls should be valid`() {
        val recentTimeControls = List(3) { 
            TimeControl(name = "Recent $it", timeInSeconds = 300, incrementInSeconds = 0)
        }
        val customTimeControls = List(5) { 
            TimeControl(name = "Custom $it", timeInSeconds = 300, incrementInSeconds = 0)
        }
        val state = GameUiState(
            recentTimeControls = recentTimeControls,
            customTimeControls = customTimeControls
        )
        assertTrue("Maximum allowed time controls should be valid", state.isValid())
    }

    @Test
    fun `getTimeControlForPlayer returns correct time control`() {
        val player1Control = TimeControl(name = "Player 1", timeInSeconds = 180, incrementInSeconds = 2)
        val player2Control = TimeControl(name = "Player 2", timeInSeconds = 600, incrementInSeconds = 5)
        
        val state = GameUiState(
            player1TimeControl = player1Control,
            player2TimeControl = player2Control
        )
        
        assertEquals(player1Control, state.getTimeControlForPlayer(Player.PLAYER_ONE))
        assertEquals(player2Control, state.getTimeControlForPlayer(Player.PLAYER_TWO))
    }

    @Test
    fun `getTimeForPlayer returns correct time`() {
        val state = GameUiState(
            player1Time = 180L,
            player2Time = 240L
        )
        
        assertEquals(180L, state.getTimeForPlayer(Player.PLAYER_ONE))
        assertEquals(240L, state.getTimeForPlayer(Player.PLAYER_TWO))
    }

    @Test
    fun `canInteractWithTimers returns correct values`() {
        // Can interact when running
        val runningState = GameUiState(gameState = GameState.RUNNING)
        assertTrue("Should be able to interact when running", runningState.canInteractWithTimers())
        
        // Can interact when paused
        val pausedState = GameUiState(gameState = GameState.PAUSED)
        assertTrue("Should be able to interact when paused", pausedState.canInteractWithTimers())
        
        // Cannot interact when stopped
        val stoppedState = GameUiState(gameState = GameState.STOPPED)
        assertFalse("Should not be able to interact when stopped", stoppedState.canInteractWithTimers())
        
        // Cannot interact when game over
        val gameOverState = GameUiState(gameState = GameState.GAME_OVER, winner = Player.PLAYER_ONE)
        assertFalse("Should not be able to interact when game over", gameOverState.canInteractWithTimers())
        
        // Cannot interact when there's a winner (even if not game over)
        val winnerState = GameUiState(gameState = GameState.RUNNING, winner = Player.PLAYER_ONE)
        assertFalse("Should not be able to interact when there's a winner", winnerState.canInteractWithTimers())
    }

    @Test
    fun `canStartGame returns correct values`() {
        val stoppedState = GameUiState(gameState = GameState.STOPPED)
        assertTrue("Should be able to start when stopped", stoppedState.canStartGame())
        
        val runningState = GameUiState(gameState = GameState.RUNNING)
        assertFalse("Should not be able to start when running", runningState.canStartGame())
        
        val pausedState = GameUiState(gameState = GameState.PAUSED)
        assertFalse("Should not be able to start when paused", pausedState.canStartGame())
        
        val gameOverState = GameUiState(gameState = GameState.GAME_OVER, winner = Player.PLAYER_ONE)
        assertFalse("Should not be able to start when game over", gameOverState.canStartGame())
    }

    @Test
    fun `canPauseGame returns correct values`() {
        val runningState = GameUiState(gameState = GameState.RUNNING)
        assertTrue("Should be able to pause when running", runningState.canPauseGame())
        
        val stoppedState = GameUiState(gameState = GameState.STOPPED)
        assertFalse("Should not be able to pause when stopped", stoppedState.canPauseGame())
        
        val pausedState = GameUiState(gameState = GameState.PAUSED)
        assertFalse("Should not be able to pause when already paused", pausedState.canPauseGame())
        
        val gameOverState = GameUiState(gameState = GameState.GAME_OVER, winner = Player.PLAYER_ONE)
        assertFalse("Should not be able to pause when game over", gameOverState.canPauseGame())
    }

    @Test
    fun `canResumeGame returns correct values`() {
        val pausedState = GameUiState(gameState = GameState.PAUSED)
        assertTrue("Should be able to resume when paused", pausedState.canResumeGame())
        
        val stoppedState = GameUiState(gameState = GameState.STOPPED)
        assertFalse("Should not be able to resume when stopped", stoppedState.canResumeGame())
        
        val runningState = GameUiState(gameState = GameState.RUNNING)
        assertFalse("Should not be able to resume when running", runningState.canResumeGame())
        
        val gameOverState = GameUiState(gameState = GameState.GAME_OVER, winner = Player.PLAYER_ONE)
        assertFalse("Should not be able to resume when game over", gameOverState.canResumeGame())
    }

    @Test
    fun `canResetGame returns correct values`() {
        val stoppedState = GameUiState(gameState = GameState.STOPPED)
        assertFalse("Should not be able to reset when stopped", stoppedState.canResetGame())
        
        val runningState = GameUiState(gameState = GameState.RUNNING)
        assertTrue("Should be able to reset when running", runningState.canResetGame())
        
        val pausedState = GameUiState(gameState = GameState.PAUSED)
        assertTrue("Should be able to reset when paused", pausedState.canResetGame())
        
        val gameOverState = GameUiState(gameState = GameState.GAME_OVER, winner = Player.PLAYER_ONE)
        assertTrue("Should be able to reset when game over", gameOverState.canResetGame())
    }

    @Test
    fun `getOpponent returns correct opponent`() {
        val state = GameUiState()
        
        assertEquals(Player.PLAYER_TWO, state.getOpponent(Player.PLAYER_ONE))
        assertEquals(Player.PLAYER_ONE, state.getOpponent(Player.PLAYER_TWO))
    }

    @Test
    fun `state transitions maintain validity`() {
        // Test typical game flow
        var state = GameUiState() // STOPPED
        assertTrue("Initial state should be valid", state.isValid())
        
        // Start game
        state = state.copy(
            gameState = GameState.RUNNING,
            activePlayer = Player.PLAYER_ONE
        )
        assertTrue("Running state should be valid", state.isValid())
        
        // Pause game
        state = state.copy(gameState = GameState.PAUSED)
        assertTrue("Paused state should be valid", state.isValid())
        
        // Resume game
        state = state.copy(gameState = GameState.RUNNING)
        assertTrue("Resumed state should be valid", state.isValid())
        
        // End game with winner
        state = state.copy(
            gameState = GameState.GAME_OVER,
            winner = Player.PLAYER_ONE,
            activePlayer = null
        )
        assertTrue("Game over state should be valid", state.isValid())
        
        // Reset game
        state = state.copy(
            gameState = GameState.STOPPED,
            winner = null,
            activePlayer = null,
            player1Time = 300L,
            player2Time = 300L
        )
        assertTrue("Reset state should be valid", state.isValid())
    }

    @Test
    fun `different time controls functionality`() {
        val player1Control = TimeControl(name = "Fast", timeInSeconds = 60, incrementInSeconds = 1)
        val player2Control = TimeControl(name = "Slow", timeInSeconds = 300, incrementInSeconds = 5)
        
        val state = GameUiState(
            player1TimeControl = player1Control,
            player2TimeControl = player2Control,
            isDifferentTimeControls = true,
            player1Time = 60L,
            player2Time = 300L
        )
        
        assertTrue("Different time controls state should be valid", state.isValid())
        assertEquals(player1Control, state.getTimeControlForPlayer(Player.PLAYER_ONE))
        assertEquals(player2Control, state.getTimeControlForPlayer(Player.PLAYER_TWO))
        assertEquals(60L, state.getTimeForPlayer(Player.PLAYER_ONE))
        assertEquals(300L, state.getTimeForPlayer(Player.PLAYER_TWO))
    }
}