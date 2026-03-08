
package com.example.gameclock.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameclock.models.DelayType
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
    private val preferencesRepository: PreferencesRepository,
    private val application: Application? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameTimer: Job? = null
    private var lastTickNanos: Long = 0L
    private var turnStartNanos: Long = 0L
    private var turnTimeSpentMs: Long = 0L

    private var wakeLock: PowerManager.WakeLock? = null

    // Backward-compatible state flows
    private val _gameState = MutableStateFlow(GameState.STOPPED)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _player1Time = MutableStateFlow(300L)
    val player1Time: StateFlow<Long> = _player1Time.asStateFlow()

    private val _player2Time = MutableStateFlow(300L)
    val player2Time: StateFlow<Long> = _player2Time.asStateFlow()

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer.asStateFlow()

    companion object {
        private const val LOW_TIME_THRESHOLD_MS = 30_000L
        private const val SHOW_DECIMALS_THRESHOLD_MS = 20_000L
    }

    init {
        viewModelScope.launch {
            loadInitialData()
        }

        viewModelScope.launch {
            uiState.collect { state ->
                _gameState.value = state.gameState
                _player1Time.value = state.player1Time
                _player2Time.value = state.player2Time
                _activePlayer.value = state.activePlayer

                // Manage wake lock based on game state
                when (state.gameState) {
                    GameState.RUNNING, GameState.PAUSED -> acquireWakeLock()
                    else -> releaseWakeLock()
                }
            }
        }
    }

    private suspend fun loadInitialData() {
        try {
            val recentTimeControls = gameRepository.getRecentTimeControls()
            val customTimeControls = gameRepository.getCustomTimeControls()
            val lastUsedTimeControl = preferencesRepository.getLastUsedTimeControl()
                ?: TimeControl.BLITZ_PRESETS[2]
            val lowTimeEnabled = preferencesRepository.getLowTimeWarningEnabled()

            _uiState.value = _uiState.value.copy(
                recentTimeControls = recentTimeControls,
                customTimeControls = customTimeControls,
                player1TimeControl = lastUsedTimeControl,
                player2TimeControl = lastUsedTimeControl,
                player1TimeMs = lastUsedTimeControl.totalTimeSeconds * 1000L,
                player2TimeMs = lastUsedTimeControl.totalTimeSeconds * 1000L,
                lowTimeWarningEnabled = lowTimeEnabled
            )
        } catch (e: Exception) {
            android.util.Log.w("GameViewModel", "Failed to load initial data", e)
            val defaultTimeControl = TimeControl.BLITZ_PRESETS[2]
            _uiState.value = _uiState.value.copy(
                player1TimeControl = defaultTimeControl,
                player2TimeControl = defaultTimeControl,
                player1TimeMs = defaultTimeControl.timeInSeconds * 1000L,
                player2TimeMs = defaultTimeControl.timeInSeconds * 1000L
            )
        }
    }

    fun startGame() {
        val currentState = _uiState.value
        if (!currentState.canStartGame()) return

        turnTimeSpentMs = 0L

        _uiState.value = currentState.copy(
            gameState = GameState.RUNNING,
            activePlayer = Player.PLAYER_ONE,
            player1Moves = 0,
            player2Moves = 0,
            player1StageIndex = 0,
            player2StageIndex = 0,
            delayRemainingMs = getInitialDelay(currentState.player1TimeControl, 0)
        )

        startTimer()
    }

    fun startGameWithPlayer(startingPlayer: Player) {
        val currentState = _uiState.value
        if (!currentState.canStartGame()) return

        turnTimeSpentMs = 0L
        val tc = currentState.getTimeControlForPlayer(startingPlayer)

        _uiState.value = currentState.copy(
            gameState = GameState.RUNNING,
            activePlayer = startingPlayer,
            player1Moves = 0,
            player2Moves = 0,
            player1StageIndex = 0,
            player2StageIndex = 0,
            delayRemainingMs = getInitialDelay(tc, 0)
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
        turnTimeSpentMs = 0L

        val p1Tc = currentState.player1TimeControl
        val p2Tc = currentState.player2TimeControl

        _uiState.value = currentState.copy(
            gameState = GameState.STOPPED,
            activePlayer = null,
            winner = null,
            player1TimeMs = p1Tc.totalTimeSeconds * 1000L,
            player2TimeMs = p2Tc.totalTimeSeconds * 1000L,
            player1Moves = 0,
            player2Moves = 0,
            player1StageIndex = 0,
            player2StageIndex = 0,
            delayRemainingMs = 0L,
            isLowTime1 = false,
            isLowTime2 = false
        )
    }

    fun switchPlayer() {
        val currentState = _uiState.value
        if (!currentState.canInteractWithTimers() || currentState.gameState != GameState.RUNNING) return

        val activePlayer = currentState.activePlayer ?: return
        val opponent = currentState.getOpponent(activePlayer)
        val tc = currentState.getTimeControlForPlayer(activePlayer)
        val stages = tc.effectiveStages
        val stageIndex = currentState.getStageIndexForPlayer(activePlayer)
        val currentStage = stages[stageIndex]
        val delayType = tc.effectiveDelayType

        // Calculate increment based on delay type
        val incrementMs = when (delayType) {
            DelayType.FISCHER -> currentStage.incrementInSeconds * 1000L
            DelayType.BRONSTEIN -> minOf(turnTimeSpentMs, currentStage.incrementInSeconds * 1000L)
            DelayType.SIMPLE_DELAY, DelayType.NONE -> 0L
        }

        // Update moves
        val newMoves = currentState.getMovesForPlayer(activePlayer) + 1

        // Check stage advancement
        var newStageIndex = stageIndex
        var bonusTimeMs = 0L
        if (currentStage.moves != null && newMoves >= currentStage.moves && stageIndex + 1 < stages.size) {
            newStageIndex = stageIndex + 1
            bonusTimeMs = stages[newStageIndex].timeInSeconds * 1000L
        }

        // Calculate new time for current player
        val currentTimeMs = currentState.getTimeMsForPlayer(activePlayer)
        val newTimeMs = currentTimeMs + incrementMs + bonusTimeMs

        // Get initial delay for opponent
        val opponentTc = currentState.getTimeControlForPlayer(opponent)
        val opponentStageIndex = currentState.getStageIndexForPlayer(opponent)
        val opponentDelay = getInitialDelay(opponentTc, opponentStageIndex)

        // Build updated state
        val updatedState = when (activePlayer) {
            Player.PLAYER_ONE -> currentState.copy(
                activePlayer = opponent,
                player1TimeMs = newTimeMs,
                player1Moves = newMoves,
                player1StageIndex = newStageIndex,
                delayRemainingMs = opponentDelay
            )
            Player.PLAYER_TWO -> currentState.copy(
                activePlayer = opponent,
                player2TimeMs = newTimeMs,
                player2Moves = newMoves,
                player2StageIndex = newStageIndex,
                delayRemainingMs = opponentDelay
            )
        }

        turnTimeSpentMs = 0L
        _uiState.value = updatedState

        startTimer()
    }

    private fun getInitialDelay(tc: TimeControl, stageIndex: Int): Long {
        if (tc.effectiveDelayType != DelayType.SIMPLE_DELAY) return 0L
        val stages = tc.effectiveStages
        val stage = stages.getOrElse(stageIndex) { stages.last() }
        return stage.incrementInSeconds * 1000L
    }

    private fun startTimer() {
        stopTimer()
        lastTickNanos = System.nanoTime()
        turnStartNanos = lastTickNanos

        gameTimer = viewModelScope.launch {
            while (true) {
                delay(100)

                val now = System.nanoTime()
                val elapsedMs = (now - lastTickNanos) / 1_000_000L
                lastTickNanos = now

                turnTimeSpentMs += elapsedMs

                updateActivePlayerTime(elapsedMs)
            }
        }
    }

    private fun stopTimer() {
        gameTimer?.cancel()
        gameTimer = null
    }

    private fun updateActivePlayerTime(elapsedMs: Long) {
        val currentState = _uiState.value
        val activePlayer = currentState.activePlayer ?: return

        var remainingElapsed = elapsedMs

        // Handle delay countdown first
        if (currentState.delayRemainingMs > 0) {
            val delayConsumed = minOf(currentState.delayRemainingMs, remainingElapsed)
            val newDelay = currentState.delayRemainingMs - delayConsumed
            remainingElapsed -= delayConsumed

            if (remainingElapsed <= 0) {
                _uiState.value = currentState.copy(delayRemainingMs = newDelay)
                return
            }

            _uiState.value = currentState.copy(delayRemainingMs = 0L)
        }

        // Decrement active player's time
        val currentTimeMs = _uiState.value.getTimeMsForPlayer(activePlayer)
        val newTimeMs = maxOf(0L, currentTimeMs - remainingElapsed)

        // Update low time flags
        val isLowTime1 = when (activePlayer) {
            Player.PLAYER_ONE -> newTimeMs in 1..LOW_TIME_THRESHOLD_MS
            Player.PLAYER_TWO -> _uiState.value.player1TimeMs in 1..LOW_TIME_THRESHOLD_MS
        }
        val isLowTime2 = when (activePlayer) {
            Player.PLAYER_TWO -> newTimeMs in 1..LOW_TIME_THRESHOLD_MS
            Player.PLAYER_ONE -> _uiState.value.player2TimeMs in 1..LOW_TIME_THRESHOLD_MS
        }

        if (newTimeMs <= 0L) {
            // Game over
            stopTimer()
            val winner = _uiState.value.getOpponent(activePlayer)
            _uiState.value = when (activePlayer) {
                Player.PLAYER_ONE -> _uiState.value.copy(
                    gameState = GameState.GAME_OVER,
                    player1TimeMs = 0L,
                    winner = winner,
                    activePlayer = null,
                    isLowTime1 = false,
                    isLowTime2 = false,
                    delayRemainingMs = 0L
                )
                Player.PLAYER_TWO -> _uiState.value.copy(
                    gameState = GameState.GAME_OVER,
                    player2TimeMs = 0L,
                    winner = winner,
                    activePlayer = null,
                    isLowTime1 = false,
                    isLowTime2 = false,
                    delayRemainingMs = 0L
                )
            }
            playGameOverSound()
        } else {
            _uiState.value = when (activePlayer) {
                Player.PLAYER_ONE -> _uiState.value.copy(
                    player1TimeMs = newTimeMs,
                    isLowTime1 = isLowTime1,
                    isLowTime2 = isLowTime2
                )
                Player.PLAYER_TWO -> _uiState.value.copy(
                    player2TimeMs = newTimeMs,
                    isLowTime1 = isLowTime1,
                    isLowTime2 = isLowTime2
                )
            }
        }
    }

    private fun playGameOverSound() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
            viewModelScope.launch {
                delay(600)
                toneGenerator.release()
            }
        } catch (e: Exception) {
            // Sound is not critical
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        try {
            val pm = application?.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "GameClock::TimerWakeLock"
            )
            wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max
        } catch (e: Exception) {
            // Wake lock is not critical
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        wakeLock = null
    }

    // Time control management
    fun setTimeControl(timeControl: TimeControl) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            player1TimeControl = timeControl,
            player2TimeControl = timeControl,
            player1TimeMs = timeControl.totalTimeSeconds * 1000L,
            player2TimeMs = timeControl.totalTimeSeconds * 1000L,
            isDifferentTimeControls = false
        )

        viewModelScope.launch {
            try {
                preferencesRepository.saveLastUsedTimeControl(timeControl)
                gameRepository.saveRecentTimeControl(timeControl)
                val updatedRecent = gameRepository.getRecentTimeControls()
                _uiState.value = _uiState.value.copy(recentTimeControls = updatedRecent)
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    fun setDifferentTimeControls(player1TimeControl: TimeControl, player2TimeControl: TimeControl) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            player1TimeControl = player1TimeControl,
            player2TimeControl = player2TimeControl,
            player1TimeMs = player1TimeControl.totalTimeSeconds * 1000L,
            player2TimeMs = player2TimeControl.totalTimeSeconds * 1000L,
            isDifferentTimeControls = true
        )

        viewModelScope.launch {
            try {
                gameRepository.saveRecentTimeControl(player1TimeControl)
                gameRepository.saveRecentTimeControl(player2TimeControl)
                val updatedRecent = gameRepository.getRecentTimeControls()
                _uiState.value = _uiState.value.copy(recentTimeControls = updatedRecent)
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    fun addCustomTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            try {
                gameRepository.saveCustomTimeControl(timeControl)
                val updatedCustom = gameRepository.getCustomTimeControls()
                _uiState.value = _uiState.value.copy(customTimeControls = updatedCustom)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCustomTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            try {
                gameRepository.deleteCustomTimeControl(timeControl)
                val updatedCustom = gameRepository.getCustomTimeControls()
                _uiState.value = _uiState.value.copy(customTimeControls = updatedCustom)
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    fun setLowTimeWarningEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(lowTimeWarningEnabled = enabled)
        viewModelScope.launch {
            try {
                preferencesRepository.saveLowTimeWarningEnabled(enabled)
            } catch (e: Exception) {
                // Handle silently
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
                // Handle silently
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        releaseWakeLock()
    }
}
