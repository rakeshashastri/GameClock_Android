package com.example.gameclock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.Player
import kotlinx.coroutines.delay

/**
 * WinnerDisplayOverlay composable that displays the winner when a game ends.
 * 
 * Requirements addressed:
 * - 1.4: Display winner when timer reaches zero
 * - 5.4: Display winner and disable timer interactions
 * - 8.4: Clear winner designation when game is reset
 */
@Composable
fun WinnerDisplayOverlay(
    winner: Player,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Animate in the winner display
    LaunchedEffect(winner) {
        delay(300) // Small delay for dramatic effect
        isVisible = true
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        
        // Winner announcement card
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                animationSpec = tween(durationMillis = 500),
                initialScale = 0.8f
            ) + fadeIn(animationSpec = tween(durationMillis = 500))
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Color(
                            when (winner) {
                                Player.PLAYER_ONE -> theme.player1Color
                                Player.PLAYER_TWO -> theme.player2Color
                            }
                        )
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Winner text
                    Text(
                        text = "WINNER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = getContrastColor(
                            Color(
                                when (winner) {
                                    Player.PLAYER_ONE -> theme.player1Color
                                    Player.PLAYER_TWO -> theme.player2Color
                                }
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Player identification
                    Text(
                        text = when (winner) {
                            Player.PLAYER_ONE -> "Player 1"
                            Player.PLAYER_TWO -> "Player 2"
                        },
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = getContrastColor(
                            Color(
                                when (winner) {
                                    Player.PLAYER_ONE -> theme.player1Color
                                    Player.PLAYER_TWO -> theme.player2Color
                                }
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Instruction text
                    Text(
                        text = "Tap Reset to play again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = getContrastColor(
                            Color(
                                when (winner) {
                                    Player.PLAYER_ONE -> theme.player1Color
                                    Player.PLAYER_TWO -> theme.player2Color
                                }
                            )
                        ).copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Determines the appropriate text color for contrast against the background
 */
private fun getContrastColor(backgroundColor: Color): Color {
    val red = backgroundColor.red
    val green = backgroundColor.green
    val blue = backgroundColor.blue
    
    // Calculate luminance using standard formula
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return if (luminance > 0.5) Color.Black else Color.White
}