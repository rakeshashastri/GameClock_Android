
package com.example.gameclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameclock.models.GameState
import com.example.gameclock.repositories.GameRepositoryImpl
import com.example.gameclock.repositories.PreferencesRepositoryImpl
import com.example.gameclock.ui.GameViewModel
import com.example.gameclock.ui.ThemeViewModel
import com.example.gameclock.ui.theme.GameClockTheme
import com.example.gameclock.ui.views.GameScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val gameRepository = GameRepositoryImpl(this@MainActivity)
            val preferencesRepository = PreferencesRepositoryImpl(this@MainActivity)

            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(this@MainActivity))
            val gameViewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(gameRepository, preferencesRepository, application)
            )

            val theme by themeViewModel.currentTheme.collectAsState()
            val gameUiState by gameViewModel.uiState.collectAsState()

            LaunchedEffect(gameUiState.gameState) {
                val keepAwake = gameUiState.gameState == GameState.RUNNING || gameUiState.gameState == GameState.PAUSED
                if (keepAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            GameClockTheme(theme = theme) {
                GameScreen(
                    gameUiState = gameUiState,
                    currentTheme = theme,
                    availableThemes = themeViewModel.availableThemes,
                    onPlayerTap = { player ->
                        when (gameUiState.gameState) {
                            GameState.RUNNING -> gameViewModel.switchPlayer()
                            GameState.PAUSED -> gameViewModel.resumeGame()
                            GameState.STOPPED -> {
                                val startingPlayer = when (player) {
                                    com.example.gameclock.models.Player.PLAYER_ONE -> com.example.gameclock.models.Player.PLAYER_TWO
                                    com.example.gameclock.models.Player.PLAYER_TWO -> com.example.gameclock.models.Player.PLAYER_ONE
                                }
                                gameViewModel.startGameWithPlayer(startingPlayer)
                            }
                            else -> { }
                        }
                    },
                    onPlayClick = {
                        when {
                            gameUiState.canStartGame() -> gameViewModel.startGame()
                            gameUiState.canResumeGame() -> gameViewModel.resumeGame()
                        }
                    },
                    onPauseClick = { gameViewModel.pauseGame() },
                    onResetClick = { gameViewModel.resetGame() },
                    onTimeControlSelected = { timeControl ->
                        gameViewModel.setTimeControl(timeControl)
                    },
                    onCustomTimeControlSave = { timeControl ->
                        gameViewModel.addCustomTimeControl(timeControl)
                    },
                    onCustomTimeControlSaveDifferent = { p1, p2 ->
                        gameViewModel.addCustomTimeControl(p1)
                        gameViewModel.setDifferentTimeControls(p1, p2)
                    },
                    onCustomTimeControlDelete = { timeControl ->
                        gameViewModel.deleteCustomTimeControl(timeControl)
                    },
                    onThemeSelected = { selectedTheme ->
                        themeViewModel.selectTheme(selectedTheme)
                    },
                    onLowTimeWarningChanged = { enabled ->
                        gameViewModel.setLowTimeWarningEnabled(enabled)
                    }
                )
            }
        }
    }
}
