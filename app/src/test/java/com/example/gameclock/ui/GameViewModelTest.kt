package com.example.gameclock.ui

import com.example.gameclock.models.GameState
import com.example.gameclock.models.Player
import com.example.gameclock.models.TimeControl
import com.example.gameclock.repositories.GameRepository
import com.example.gameclock.repositories.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private lateinit var gameRepository: GameRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: GameViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    private val testTimeControl = TimeControl(
        id = "test",
        name = "Test 5+3",
        timeInSeconds = 300L,
        incrementInSeconds = 3L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        gameRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        
        // Setup default mock responses
        coEvery { gameRepository.getRecentTimeControls() } returns emptyList()
        coEvery { gameRepository.getCustomTimeControls() } returns emptyList()
        coEvery { preferencesRepository.getLastUsedTimeControl() } returns testTimeControl
        
        viewModel = GameViewModel(gameRepository, preferencesRepository)
    }

    @Test
    fun `initial state should be stopped with default values`() = runTest {
        // Allow initialization to complete
        advanceTimeBy(100)
        
        val state = viewModel.uiState.value
        
        assertEquals(GameState.STOPPED, state.gameState)
        assertNull(state.activePlayer)
        assertNull(state.winner)
        assertEquals(300L, state.player1Time)
        assertEquals(300L, state.player2Time)
        assertEquals(testTimeControl, state.player1TimeControl)
        assertEquals(testTimeControl, state.player2TimeControl)
    }

    @Test
    fun `startGame should set state to running with player one active`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        
        val state = viewModel.uiState.value
        assertEquals(GameState.RUNNING, state.gameState)
        assertEquals(Player.PLAYER_ONE, state.activePlayer)
    }

    @Test
    fun `pauseGame should set state to paused when running`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        viewModel.pauseGame()
        
        val state = viewModel.uiState.value
        assertEquals(GameState.PAUSED, state.gameState)
        assertEquals(Player.PLAYER_ONE, state.activePlayer) // Should maintain active player
    }

    @Test
    fun `pauseGame should not work when game is stopped`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.pauseGame() // Try to pause when stopped
        
        val state = viewModel.uiState.value
        assertEquals(GameState.STOPPED, state.gameState) // Should remain stopped
    }

    @Test
    fun `resumeGame should set state to running when paused`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        viewModel.pauseGame()
        viewModel.resumeGame()
        
        val state = viewModel.uiState.value
        assertEquals(GameState.RUNNING, state.gameState)
        assertEquals(Player.PLAYER_ONE, state.activePlayer)
    }

    @Test
    fun `resetGame should restore initial state`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        viewModel.resetGame()
        
        val state = viewModel.uiState.value
        assertEquals(GameState.STOPPED, state.gameState)
        assertNull(state.activePlayer)
        assertNull(state.winner)
        assertEquals(300L, state.player1Time) // Should restore original time
        assertEquals(300L, state.player2Time)
    }

    @Test
    fun `switchPlayer should change active player and apply increment`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        val initialState = viewModel.uiState.value
        val initialPlayer1Time = initialState.player1Time
        
        viewModel.switchPlayer()
        
        val state = viewModel.uiState.value
        assertEquals(Player.PLAYER_TWO, state.activePlayer)
        assertEquals(initialPlayer1Time + 3L, state.player1Time) // Should add increment
    }

    @Test
    fun `switchPlayer should not work when game is stopped`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.switchPlayer() // Try to switch when stopped
        
        val state = viewModel.uiState.value
        assertNull(state.activePlayer) // Should remain null
    }

    @Test
    fun `timer should decrement active player time`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        val initialTime = viewModel.uiState.value.player1Time
        
        // Advance time to trigger timer updates
        advanceTimeBy(2000) // 2 seconds
        
        val state = viewModel.uiState.value
        assertTrue("Player 1 time should have decreased", state.player1Time < initialTime)
    }

    @Test
    fun `game should end when player time reaches zero`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        // Create a short time control for testing
        val shortTimeControl = TimeControl(
            id = "short",
            name = "Short",
            timeInSeconds = 1L,
            incrementInSeconds = 0L
        )
        
        // Mock the repository to return the short time control
        coEvery { preferencesRepository.getLastUsedTimeControl() } returns shortTimeControl
        
        // Create new viewModel with short time
        viewModel = GameViewModel(gameRepository, preferencesRepository)
        advanceTimeBy(100) // Allow initialization
        
        viewModel.startGame()
        
        // Advance time beyond the player's time
        advanceTimeBy(2000) // 2 seconds, more than the 1 second available
        
        val state = viewModel.uiState.value
        assertEquals(GameState.GAME_OVER, state.gameState)
        assertEquals(Player.PLAYER_TWO, state.winner) // Player 2 should win when Player 1 runs out
        assertEquals(0L, state.player1Time)
    }

    @Test
    fun `state validation should work correctly`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val state = viewModel.uiState.value
        assertTrue("Initial state should be valid", state.isValid())
        
        // Test canStartGame
        assertTrue("Should be able to start game when stopped", state.canStartGame())
        
        // Test canPauseGame
        assertFalse("Should not be able to pause when stopped", state.canPauseGame())
        
        // Test canResetGame
        assertFalse("Should not be able to reset when stopped", state.canResetGame())
    }

    @Test
    fun `getTimeControlForPlayer should return correct time control`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val state = viewModel.uiState.value
        
        assertEquals(testTimeControl, state.getTimeControlForPlayer(Player.PLAYER_ONE))
        assertEquals(testTimeControl, state.getTimeControlForPlayer(Player.PLAYER_TWO))
    }

    @Test
    fun `getTimeForPlayer should return correct time`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val state = viewModel.uiState.value
        
        assertEquals(300L, state.getTimeForPlayer(Player.PLAYER_ONE))
        assertEquals(300L, state.getTimeForPlayer(Player.PLAYER_TWO))
    }

    @Test
    fun `getOpponent should return correct opponent`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val state = viewModel.uiState.value
        
        assertEquals(Player.PLAYER_TWO, state.getOpponent(Player.PLAYER_ONE))
        assertEquals(Player.PLAYER_ONE, state.getOpponent(Player.PLAYER_TWO))
    }

    @Test
    fun `initialization should load data from repositories`() = runTest {
        val recentTimeControls = listOf(testTimeControl)
        val customTimeControls = listOf(testTimeControl)
        
        coEvery { gameRepository.getRecentTimeControls() } returns recentTimeControls
        coEvery { gameRepository.getCustomTimeControls() } returns customTimeControls
        
        val newViewModel = GameViewModel(gameRepository, preferencesRepository)
        advanceTimeBy(100) // Allow initialization
        
        val state = newViewModel.uiState.value
        assertEquals(recentTimeControls, state.recentTimeControls)
        assertEquals(customTimeControls, state.customTimeControls)
        
        coVerify { gameRepository.getRecentTimeControls() }
        coVerify { gameRepository.getCustomTimeControls() }
        coVerify { preferencesRepository.getLastUsedTimeControl() }
    }

    @Test
    fun `initialization should handle repository errors gracefully`() = runTest {
        coEvery { gameRepository.getRecentTimeControls() } throws Exception("Test error")
        coEvery { gameRepository.getCustomTimeControls() } throws Exception("Test error")
        coEvery { preferencesRepository.getLastUsedTimeControl() } throws Exception("Test error")
        
        val newViewModel = GameViewModel(gameRepository, preferencesRepository)
        advanceTimeBy(100) // Allow initialization
        
        val state = newViewModel.uiState.value
        // Should use default values when repositories fail
        assertTrue(state.recentTimeControls.isEmpty())
        assertTrue(state.customTimeControls.isEmpty())
        assertEquals(TimeControl.BLITZ_PRESETS[2], state.player1TimeControl)
    }

    // Time Control Management Tests

    @Test
    fun `setTimeControl should update both players with same time control`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val newTimeControl = TimeControl(
            id = "new",
            name = "New 10+5",
            timeInSeconds = 600L,
            incrementInSeconds = 5L
        )
        
        viewModel.setTimeControl(newTimeControl)
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertEquals(newTimeControl, state.player1TimeControl)
        assertEquals(newTimeControl, state.player2TimeControl)
        assertEquals(600L, state.player1Time)
        assertEquals(600L, state.player2Time)
        assertFalse(state.isDifferentTimeControls)
        
        coVerify { preferencesRepository.saveLastUsedTimeControl(newTimeControl) }
        coVerify { gameRepository.saveRecentTimeControl(newTimeControl) }
    }

    @Test
    fun `setDifferentTimeControls should set different time controls for each player`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val player1TimeControl = TimeControl(
            id = "p1",
            name = "Player 1 - 5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        val player2TimeControl = TimeControl(
            id = "p2",
            name = "Player 2 - 10+5",
            timeInSeconds = 600L,
            incrementInSeconds = 5L
        )
        
        viewModel.setDifferentTimeControls(player1TimeControl, player2TimeControl)
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertEquals(player1TimeControl, state.player1TimeControl)
        assertEquals(player2TimeControl, state.player2TimeControl)
        assertEquals(300L, state.player1Time)
        assertEquals(600L, state.player2Time)
        assertTrue(state.isDifferentTimeControls)
        
        coVerify { gameRepository.saveRecentTimeControl(player1TimeControl) }
        coVerify { gameRepository.saveRecentTimeControl(player2TimeControl) }
    }

    @Test
    fun `addCustomTimeControl should add time control to custom list`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val customTimeControl = TimeControl(
            id = "custom",
            name = "Custom 7+2",
            timeInSeconds = 420L,
            incrementInSeconds = 2L
        )
        
        val updatedCustomList = listOf(customTimeControl)
        coEvery { gameRepository.getCustomTimeControls() } returns updatedCustomList
        
        viewModel.addCustomTimeControl(customTimeControl)
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertEquals(updatedCustomList, state.customTimeControls)
        
        coVerify { gameRepository.saveCustomTimeControl(customTimeControl) }
        coVerify { gameRepository.getCustomTimeControls() }
    }

    @Test
    fun `deleteCustomTimeControl should remove time control from custom list`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val customTimeControl = TimeControl(
            id = "custom",
            name = "Custom 7+2",
            timeInSeconds = 420L,
            incrementInSeconds = 2L
        )
        
        // Initially have the custom time control
        val initialCustomList = listOf(customTimeControl)
        coEvery { gameRepository.getCustomTimeControls() } returns initialCustomList
        
        viewModel.refreshTimeControls()
        advanceTimeBy(100)
        
        // Now remove it
        coEvery { gameRepository.getCustomTimeControls() } returns emptyList()
        
        viewModel.deleteCustomTimeControl(customTimeControl)
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertTrue(state.customTimeControls.isEmpty())
        
        coVerify { gameRepository.deleteCustomTimeControl(customTimeControl) }
    }

    @Test
    fun `refreshTimeControls should update both recent and custom time controls`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val recentTimeControls = listOf(testTimeControl)
        val customTimeControls = listOf(
            TimeControl(id = "custom1", name = "Custom 1", timeInSeconds = 180L, incrementInSeconds = 1L)
        )
        
        coEvery { gameRepository.getRecentTimeControls() } returns recentTimeControls
        coEvery { gameRepository.getCustomTimeControls() } returns customTimeControls
        
        viewModel.refreshTimeControls()
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertEquals(recentTimeControls, state.recentTimeControls)
        assertEquals(customTimeControls, state.customTimeControls)
        
        coVerify { gameRepository.getRecentTimeControls() }
        coVerify { gameRepository.getCustomTimeControls() }
    }

    @Test
    fun `setTimeControl should update recent time controls in UI state`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val newTimeControl = TimeControl(
            id = "new",
            name = "New 3+2",
            timeInSeconds = 180L,
            incrementInSeconds = 2L
        )
        
        val updatedRecentList = listOf(newTimeControl, testTimeControl)
        coEvery { gameRepository.getRecentTimeControls() } returns updatedRecentList
        
        viewModel.setTimeControl(newTimeControl)
        advanceTimeBy(100) // Allow async operations
        
        val state = viewModel.uiState.value
        assertEquals(updatedRecentList, state.recentTimeControls)
    }

    @Test
    fun `time control operations should handle repository errors gracefully`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val newTimeControl = TimeControl(
            id = "error",
            name = "Error Control",
            timeInSeconds = 300L,
            incrementInSeconds = 0L
        )
        
        // Make repository operations fail
        coEvery { gameRepository.saveRecentTimeControl(any()) } throws Exception("Save failed")
        coEvery { preferencesRepository.saveLastUsedTimeControl(any()) } throws Exception("Save failed")
        
        // Should not throw exception
        viewModel.setTimeControl(newTimeControl)
        advanceTimeBy(100)
        
        // Time control should still be set in UI state
        val state = viewModel.uiState.value
        assertEquals(newTimeControl, state.player1TimeControl)
        assertEquals(newTimeControl, state.player2TimeControl)
    }

    @Test
    fun `addCustomTimeControl should handle repository limit errors gracefully`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val customTimeControl = TimeControl(
            id = "limit",
            name = "Limit Test",
            timeInSeconds = 300L,
            incrementInSeconds = 0L
        )
        
        // Make repository throw limit exception
        coEvery { gameRepository.saveCustomTimeControl(any()) } throws IllegalStateException("Limit reached")
        
        // Should not throw exception
        viewModel.addCustomTimeControl(customTimeControl)
        advanceTimeBy(100)
        
        // Should have attempted to save
        coVerify { gameRepository.saveCustomTimeControl(customTimeControl) }
    }

    @Test
    fun `switchPlayer should apply correct increment based on player time control`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val player1TimeControl = TimeControl(
            id = "p1",
            name = "Player 1 - 5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        val player2TimeControl = TimeControl(
            id = "p2",
            name = "Player 2 - 5+5",
            timeInSeconds = 300L,
            incrementInSeconds = 5L
        )
        
        viewModel.setDifferentTimeControls(player1TimeControl, player2TimeControl)
        advanceTimeBy(100)
        
        viewModel.startGame()
        val initialState = viewModel.uiState.value
        val initialPlayer1Time = initialState.player1Time
        
        viewModel.switchPlayer()
        
        val state = viewModel.uiState.value
        assertEquals(Player.PLAYER_TWO, state.activePlayer)
        assertEquals(initialPlayer1Time + 3L, state.player1Time) // Should add Player 1's increment
        
        // Switch back to Player 1
        val initialPlayer2Time = state.player2Time
        viewModel.switchPlayer()
        
        val finalState = viewModel.uiState.value
        assertEquals(Player.PLAYER_ONE, finalState.activePlayer)
        assertEquals(initialPlayer2Time + 5L, finalState.player2Time) // Should add Player 2's increment
    }

    // Increment Functionality Tests

    @Test
    fun `switchPlayer should apply increment to player who just moved`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val timeControlWithIncrement = TimeControl(
            id = "increment",
            name = "5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        viewModel.setTimeControl(timeControlWithIncrement)
        advanceTimeBy(100)
        
        viewModel.startGame()
        val initialState = viewModel.uiState.value
        val initialPlayer1Time = initialState.player1Time
        
        // Player 1 makes a move (switches to Player 2)
        viewModel.switchPlayer()
        
        val state = viewModel.uiState.value
        assertEquals(Player.PLAYER_TWO, state.activePlayer)
        assertEquals(initialPlayer1Time + 3L, state.player1Time) // Player 1 gets increment
        assertEquals(300L, state.player2Time) // Player 2 time unchanged
    }

    @Test
    fun `switchPlayer should not apply increment when increment is zero`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val timeControlNoIncrement = TimeControl(
            id = "no_increment",
            name = "5+0",
            timeInSeconds = 300L,
            incrementInSeconds = 0L
        )
        
        viewModel.setTimeControl(timeControlNoIncrement)
        advanceTimeBy(100)
        
        viewModel.startGame()
        val initialState = viewModel.uiState.value
        val initialPlayer1Time = initialState.player1Time
        
        viewModel.switchPlayer()
        
        val state = viewModel.uiState.value
        assertEquals(Player.PLAYER_TWO, state.activePlayer)
        assertEquals(initialPlayer1Time, state.player1Time) // No increment added
    }

    @Test
    fun `resetGame should not apply increment to initial times`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val timeControlWithIncrement = TimeControl(
            id = "increment",
            name = "5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        viewModel.setTimeControl(timeControlWithIncrement)
        advanceTimeBy(100)
        
        viewModel.startGame()
        viewModel.switchPlayer() // This should add increment
        
        val stateAfterSwitch = viewModel.uiState.value
        assertTrue("Player 1 should have increment after switch", stateAfterSwitch.player1Time > 300L)
        
        viewModel.resetGame()
        
        val stateAfterReset = viewModel.uiState.value
        assertEquals(GameState.STOPPED, stateAfterReset.gameState)
        assertEquals(300L, stateAfterReset.player1Time) // Should be original time, no increment
        assertEquals(300L, stateAfterReset.player2Time) // Should be original time, no increment
        assertNull(stateAfterReset.activePlayer)
        assertNull(stateAfterReset.winner)
    }

    @Test
    fun `increment should work with different time controls for each player`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val player1TimeControl = TimeControl(
            id = "p1",
            name = "Player 1 - 3+2",
            timeInSeconds = 180L,
            incrementInSeconds = 2L
        )
        
        val player2TimeControl = TimeControl(
            id = "p2",
            name = "Player 2 - 5+5",
            timeInSeconds = 300L,
            incrementInSeconds = 5L
        )
        
        viewModel.setDifferentTimeControls(player1TimeControl, player2TimeControl)
        advanceTimeBy(100)
        
        viewModel.startGame()
        
        // Player 1 moves (gets 2 second increment)
        viewModel.switchPlayer()
        val stateAfterP1Move = viewModel.uiState.value
        assertEquals(182L, stateAfterP1Move.player1Time) // 180 + 2
        assertEquals(300L, stateAfterP1Move.player2Time) // unchanged
        assertEquals(Player.PLAYER_TWO, stateAfterP1Move.activePlayer)
        
        // Player 2 moves (gets 5 second increment)
        viewModel.switchPlayer()
        val stateAfterP2Move = viewModel.uiState.value
        assertEquals(182L, stateAfterP2Move.player1Time) // unchanged
        assertEquals(305L, stateAfterP2Move.player2Time) // 300 + 5
        assertEquals(Player.PLAYER_ONE, stateAfterP2Move.activePlayer)
    }

    @Test
    fun `increment should not be applied when game is paused or stopped`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val timeControlWithIncrement = TimeControl(
            id = "increment",
            name = "5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        viewModel.setTimeControl(timeControlWithIncrement)
        advanceTimeBy(100)
        
        // Try to switch when game is stopped
        viewModel.switchPlayer()
        val stateWhenStopped = viewModel.uiState.value
        assertEquals(GameState.STOPPED, stateWhenStopped.gameState)
        assertNull(stateWhenStopped.activePlayer)
        assertEquals(300L, stateWhenStopped.player1Time) // No increment
        
        // Start game, then pause, then try to switch
        viewModel.startGame()
        viewModel.pauseGame()
        val initialTimeWhenPaused = viewModel.uiState.value.player1Time
        
        viewModel.switchPlayer()
        val stateWhenPaused = viewModel.uiState.value
        assertEquals(GameState.PAUSED, stateWhenPaused.gameState)
        assertEquals(initialTimeWhenPaused, stateWhenPaused.player1Time) // No increment applied
    }

    @Test
    fun `increment should accumulate over multiple moves`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val timeControlWithIncrement = TimeControl(
            id = "increment",
            name = "5+2",
            timeInSeconds = 300L,
            incrementInSeconds = 2L
        )
        
        viewModel.setTimeControl(timeControlWithIncrement)
        advanceTimeBy(100)
        
        viewModel.startGame()
        
        // Make several moves for Player 1
        var expectedPlayer1Time = 300L
        
        // Move 1: Player 1 -> Player 2
        viewModel.switchPlayer()
        expectedPlayer1Time += 2L
        assertEquals(expectedPlayer1Time, viewModel.uiState.value.player1Time)
        
        // Move 2: Player 2 -> Player 1
        viewModel.switchPlayer()
        var expectedPlayer2Time = 300L + 2L // Player 2 gets increment
        assertEquals(expectedPlayer2Time, viewModel.uiState.value.player2Time)
        
        // Move 3: Player 1 -> Player 2
        viewModel.switchPlayer()
        expectedPlayer1Time += 2L
        assertEquals(expectedPlayer1Time, viewModel.uiState.value.player1Time)
        assertEquals(304L, viewModel.uiState.value.player1Time) // 300 + 2 + 2
    }

    @Test
    fun `increment should work correctly when time control is changed mid-game`() = runTest {
        advanceTimeBy(100) // Allow initialization
        
        val initialTimeControl = TimeControl(
            id = "initial",
            name = "5+1",
            timeInSeconds = 300L,
            incrementInSeconds = 1L
        )
        
        viewModel.setTimeControl(initialTimeControl)
        advanceTimeBy(100)
        
        viewModel.startGame()
        viewModel.switchPlayer() // Player 1 gets 1 second increment
        
        val stateAfterFirstMove = viewModel.uiState.value
        assertEquals(301L, stateAfterFirstMove.player1Time)
        
        // Change time control (this would reset the game in practice)
        val newTimeControl = TimeControl(
            id = "new",
            name = "5+3",
            timeInSeconds = 300L,
            incrementInSeconds = 3L
        )
        
        viewModel.setTimeControl(newTimeControl)
        advanceTimeBy(100)
        
        // Start new game with new time control
        viewModel.startGame()
        viewModel.switchPlayer() // Player 1 should now get 3 second increment
        
        val stateWithNewTimeControl = viewModel.uiState.value
        assertEquals(303L, stateWithNewTimeControl.player1Time) // 300 + 3 (new increment)
    }
}