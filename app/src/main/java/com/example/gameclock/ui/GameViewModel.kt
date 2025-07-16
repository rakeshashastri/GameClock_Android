
package com.example.gameclock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameclock.models.GameState
import com.example.gameclock.models.GameUiState
import com.example.gameclock.models.Player
import com.example.gameclock.models.TimeControl
import com.example.gameclock.repositories.GameRepository
import com.example.gameclock.repositories.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameRepository: GameRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameTimer: Job? = null
    private var lastUpdateTime: Long = 0L
    private var gameStartTime: Long = 0L
    private var initialPlayer1Time: Long = 0L
    private var initialPlayer2Time: Long = 0L

    // Expose individual state properties for backward compatibility
    private val _gameState = MutableStateFlow(GameState.STOPPED)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _player1Time = MutableStateFlow(300L)
    val player1Time: StateFlow<Long> = _player1Time.asStateFlow()
    
    private val _player2Time = MutableStateFlow(300L)
    val player2Time: StateFlow<Long> = _player2Time.asStateFlow()
    
    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer.asStateFlow()

    init {
        // Load initial data
        viewModelScope.launch {
            loadInitialData()
        }
        
        // Keep backward compatibility properties in sync
        viewModelScope.launch {
            uiState.collect { state ->
                _gameState.value = state.gameState
                _player1Time.value = state.player1Time
                _player2Time.value = state.player2Time
                _activePlayer.value = state.activePlayer
            }
        }
    }

    private suspend fun loadInitialData() {
        try {
            val recentTimeControls = gameRepository.getRecentTimeControls()
            val customTimeControls = gameRepository.getCustomTimeControls()
            val lastUsedTimeControl = preferencesRepository.getLastUsedTimeControl()
                ?: TimeControl.BLITZ_PRESETS[2] // Default to 5 min

            _uiState.value = _uiState.value.copy(
                recentTimeControls = recentTimeControls,
                customTimeControls = customTimeControls,
                player1TimeControl = lastUsedTimeControl,
                player2TimeControl = lastUsedTimeControl,
                player1Time = lastUsedTimeControl.timeInSeconds,
                player2Time = lastUsedTimeControl.timeInSeconds
            )
        } catch (e: Exception) {
            // Log error for debugging but continue with default values
            android.util.Log.w("GameViewModel", "Failed to load initial data", e)
            // Ensure we have valid default state even if loading fails
            val defaultTimeControl = TimeControl.BLITZ_PRESETS[2]
            _uiState.value = _uiState.value.copy(
                player1TimeControl = defaultTimeControl,
                player2TimeControl = defaultTimeControl,
                player1Time = defaultTimeControl.timeInSeconds,
                player2Time = defaultTimeControl.timeInSeconds
            )
        }
    }

    fun startGame() {
        val currentState = _uiState.value
        if (!currentState.canStartGame()) return

        // Initialize timer tracking variables
        gameStartTime = System.currentTimeMillis()
        initialPlayer1Time = currentState.player1Time
        initialPlayer2Time = currentState.player2Time

        _uiState.value = currentState.copy(
            gameState = GameState.RUNNING,
            activePlayer = Player.PLAYER_ONE
        )
        
        startTimer()
    }

    fun pauseGame() {
        val currentState = _uiState.value
        if (!currentState.canPauseGame()) return

        stopTimer()
        _uiState.value = currentState.copy(gameState = GameState.PAUSED)
    }

    fun resumeGame() {
        val currentState = _uiState.value
        if (!currentState.canResumeGame()) return

        _uiState.value = currentState.copy(gameState = GameState.RUNNING)
        startTimer()
    }

    fun resetGame() {
        val currentState = _uiState.value
        if (!currentState.canResetGame()) return

        stopTimer()
        
        _uiState.value = currentState.copy(
            gameState = GameState.STOPPED,
            activePlayer = null,
            winner = null,
            player1Time = currentState.player1TimeControl.timeInSeconds,
            player2Time = currentState.player2TimeControl.timeInSeconds
        )
    }

    fun switchPlayer() {
        val currentState = _uiState.value
        if (!currentState.canInteractWithTimers() || currentState.gameState != GameState.RUNNING) return

        val activePlayer = currentState.activePlayer ?: return
        val opponent = currentState.getOpponent(activePlayer)

        // Apply increment to the player who just finished their turn
        val updatedState = when (activePlayer) {
            Player.PLAYER_ONE -> {
                val increment = currentState.player1TimeControl.incrementInSeconds
                currentState.copy(
                    activePlayer = opponent,
                    player1Time = currentState.player1Time + increment
                )
            }
            Player.PLAYER_TWO -> {
                val increment = currentState.player2TimeControl.incrementInSeconds
                currentState.copy(
                    activePlayer = opponent,
                    player2Time = currentState.player2Time + increment
                )
            }
        }

        _uiState.value = updatedState
        
        // Restart timer for the new active player
        startTimer()
    }

    private fun startTimer() {
        stopTimer() // Stop any existing timer
        lastUpdateTime = System.currentTimeMillis()
        
        gameTimer = viewModelScope.launch {
            while (true) {
                delay(100) // Update every 100ms for smooth UI
                
                val currentTime = System.currentTimeMillis()
                val elapsedSeconds = (currentTime - lastUpdateTime) / 1000.0
                
                if (elapsedSeconds >= 1.0) {
                    updateActivePlayerTime()
                    lastUpdateTime = currentTime
                }
            }
        }
    }

    private fun stopTimer() {
        gameTimer?.cancel()
        gameTimer = null
    }

    private fun updateActivePlayerTime() {
        val currentState = _uiState.value
        val activePlayer = currentState.activePlayer ?: return

        val updatedState = when (activePlayer) {
            Player.PLAYER_ONE -> {
                val newTime = maxOf(0L, currentState.player1Time - 1)
                if (newTime == 0L) {
                    // Player 1 time is up, Player 2 wins
                    stopTimer()
                    currentState.copy(
                        gameState = GameState.GAME_OVER,
                        player1Time = 0L,
                        winner = Player.PLAYER_TWO,
                        activePlayer = null
                    )
                } else {
                    currentState.copy(player1Time = newTime)
                }
            }
            Player.PLAYER_TWO -> {
                val newTime = maxOf(0L, currentState.player2Time - 1)
                if (newTime == 0L) {
                    // Player 2 time is up, Player 1 wins
                    stopTimer()
                    currentState.copy(
                        gameState = GameState.GAME_OVER,
                        player2Time = 0L,
                        winner = Player.PLAYER_ONE,
                        activePlayer = null
                    )
                } else {
                    currentState.copy(player2Time = newTime)
                }
            }
        }

        _uiState.value = updatedState
    }

    // Time control management methods
    fun setTimeControl(timeControl: TimeControl) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            player1TimeControl = timeControl,
            player2TimeControl = timeControl,
            player1Time = timeControl.timeInSeconds,
            player2Time = timeControl.timeInSeconds,
            isDifferentTimeControls = false
        )
        
        // Save as last used time control and add to recent list
        viewModelScope.launch {
            try {
                preferencesRepository.saveLastUsedTimeControl(timeControl)
                gameRepository.saveRecentTimeControl(timeControl)
                
                // Update recent time controls in UI state
                val updatedRecent = gameRepository.getRecentTimeControls()
                _uiState.value = _uiState.value.copy(recentTimeControls = updatedRecent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun setDifferentTimeControls(player1TimeControl: TimeControl, player2TimeControl: TimeControl) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            player1TimeControl = player1TimeControl,
            player2TimeControl = player2TimeControl,
            player1Time = player1TimeControl.timeInSeconds,
            player2Time = player2TimeControl.timeInSeconds,
            isDifferentTimeControls = true
        )
        
        // Save both time controls to recent list
        viewModelScope.launch {
            try {
                gameRepository.saveRecentTimeControl(player1TimeControl)
                gameRepository.saveRecentTimeControl(player2TimeControl)
                
                // Update recent time controls in UI state
                val updatedRecent = gameRepository.getRecentTimeControls()
                _uiState.value = _uiState.value.copy(recentTimeControls = updatedRecent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun addCustomTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            try {
                gameRepository.saveCustomTimeControl(timeControl)
                
                // Update custom time controls in UI state
                val updatedCustom = gameRepository.getCustomTimeControls()
                _uiState.value = _uiState.value.copy(customTimeControls = updatedCustom)
            } catch (e: Exception) {
                // Handle error - could be due to reaching the limit
                // In a real app, you might want to show an error message to the user
            }
        }
    }

    fun deleteCustomTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            try {
                gameRepository.deleteCustomTimeControl(timeControl)
                
                // Update custom time controls in UI state
                val updatedCustom = gameRepository.getCustomTimeControls()
                _uiState.value = _uiState.value.copy(customTimeControls = updatedCustom)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun refreshTimeControls() {
        viewModelScope.launch {
            try {
                val recentTimeControls = gameRepository.getRecentTimeControls()
                val customTimeControls = gameRepository.getCustomTimeControls()
                
                _uiState.value = _uiState.value.copy(
                    recentTimeControls = recentTimeControls,
                    customTimeControls = customTimeControls
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
