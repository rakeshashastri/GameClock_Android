
package com.example.gameclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        
        // Enable edge-to-edge display for modern Android devices
        enableEdgeToEdge()
        
        setContent {
            // Create repositories
            val gameRepository = GameRepositoryImpl(this@MainActivity)
            val preferencesRepository = PreferencesRepositoryImpl(this@MainActivity)
            
            // Get ViewModels using factory
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(this@MainActivity))
            val gameViewModel: GameViewModel = viewModel(factory = GameViewModelFactory(gameRepository, preferencesRepository))
            
            // Collect state from ViewModels
            val theme by themeViewModel.currentTheme.collectAsState()
            val gameUiState by gameViewModel.uiState.collectAsState()
            
            // Apply theme and render main screen
            GameClockTheme(theme = theme) {
                GameScreen(
                    gameUiState = gameUiState,
                    currentTheme = theme,
                    availableThemes = themeViewModel.availableThemes,
                    onPlayerTap = { player ->
                        // Handle player tap based on game state
                        when (gameUiState.gameState) {
                            GameState.RUNNING -> gameViewModel.switchPlayer()
                            GameState.PAUSED -> gameViewModel.resumeGame()
                            else -> { /* No action for stopped/game over states */ }
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
                    onCustomTimeControlDelete = { timeControl ->
                        gameViewModel.deleteCustomTimeControl(timeControl)
                    },
                    onThemeSelected = { selectedTheme ->
                        themeViewModel.selectTheme(selectedTheme)
                    }
                )
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Automatically pause the game when app goes to background to prevent unfair time loss
        // This will be handled by the GameViewModel's lifecycle awareness
    }
    
    override fun onResume() {
        super.onResume()
        // Resume is handled by user interaction, not automatically
        // This prevents accidental resumption when returning to the app
    }
}
