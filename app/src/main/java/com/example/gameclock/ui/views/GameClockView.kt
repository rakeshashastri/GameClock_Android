
package com.example.gameclock.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gameclock.ui.GameViewModel
import com.example.gameclock.models.Player

@Composable
fun GameClockView(gameViewModel: GameViewModel, onNavigateToSettings: () -> Unit, onNavigateToTimeControl: () -> Unit) {
    Column {
        // Player 2 View (Top)
        PlayerView(gameViewModel, Player.PLAYER_TWO, Modifier.weight(1f))
        // Control Buttons
        ControlButtonsView(gameViewModel, onNavigateToSettings, onNavigateToTimeControl)
        // Player 1 View (Bottom)
        PlayerView(gameViewModel, Player.PLAYER_ONE, Modifier.weight(1f))
    }
}
