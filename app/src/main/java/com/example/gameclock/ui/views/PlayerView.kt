
package com.example.gameclock.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.gameclock.ui.GameViewModel
import com.example.gameclock.models.GameState
import com.example.gameclock.models.Player

@Composable
fun PlayerView(gameViewModel: GameViewModel, player: Player, modifier: Modifier = Modifier) {
    val gameState by gameViewModel.gameState.collectAsState()
    val time by (if (player == Player.PLAYER_ONE) gameViewModel.player1Time else gameViewModel.player2Time).collectAsState()
    val activePlayer by gameViewModel.activePlayer.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (player == Player.PLAYER_ONE) Color.Blue else Color.Red)
            .clickable {
                when (gameState) {
                    GameState.RUNNING -> if (activePlayer == player) gameViewModel.switchPlayer()
                    GameState.PAUSED -> gameViewModel.resumeGame()
                    else -> {}
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatTime(time),
            style = TextStyle(fontSize = 80.sp),
            modifier = if (player == Player.PLAYER_TWO) Modifier.rotate(180f) else Modifier
        )
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
