
package com.example.gameclock.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.gameclock.ui.GameViewModel
import com.example.gameclock.models.GameState

@Composable
fun ControlButtonsView(gameViewModel: GameViewModel, onNavigateToSettings: () -> Unit, onNavigateToTimeControl: () -> Unit) {
    val gameState by gameViewModel.gameState.collectAsState()

    Row {
        when (gameState) {
            GameState.RUNNING -> {
                Button(onClick = { gameViewModel.pauseGame() }) {
                    Text("Pause")
                }
            }
            GameState.STOPPED, GameState.PAUSED -> {
                Button(onClick = { onNavigateToSettings() }) {
                    Text("Settings")
                }
                Button(onClick = { gameViewModel.startGame() }) {
                    Text("Play")
                }
                if (gameState == GameState.PAUSED) {
                    Button(onClick = { gameViewModel.resetGame() }) {
                        Text("Reset")
                    }
                } else {
                    Button(onClick = { onNavigateToTimeControl() }) {
                        Text("Time Control")
                    }
                }
            }
            GameState.GAME_OVER -> {}
        }
    }
}
