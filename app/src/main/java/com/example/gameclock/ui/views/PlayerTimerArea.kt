package com.example.gameclock.ui.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.GameState
import com.example.gameclock.models.Player
import com.example.gameclock.ui.theme.LocalGameColors

/**
 * PlayerTimerArea composable that displays a player's timer with visual state changes
 * and touch handling for game interactions.
 * 
 * Requirements addressed:
 * - 1.1: Display timer areas for both players
 * - 1.3: Handle touch interactions for switching turns
 * - 1.6: Resume game when tapping timer areas while paused
 * - 5.1: Equal size display when no timer is active
 * - 5.2: Expand active player area to 70% of screen height
 * - 5.3: Reduce opacity to 70% when paused
 * - 7.2: Provide haptic feedback for touch interactions
 * - 7.3: Use large, monospaced fonts for readability
 */
@Composable
fun PlayerTimerArea(
    player: Player,
    timeInSeconds: Long,
    gameState: GameState,
    activePlayer: Player?,
    isActive: Boolean,
    isPaused: Boolean,
    winner: Player?,
    theme: AppTheme, // Keep for backward compatibility, but use LocalGameColors internally
    onPlayerTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val gameColors = LocalGameColors.current
    
    // Calculate responsive font size based on screen width (Requirement 7.5)
    val screenWidth = configuration.screenWidthDp
    val fontSize = when {
        screenWidth < 360 -> 56.sp // Small screens
        screenWidth < 480 -> 64.sp // Medium screens
        else -> 72.sp // Large screens
    }
    
    // Calculate visual states
    val shouldShowPausedState = isPaused && gameState == GameState.PAUSED
    
    // Animate opacity for paused state (Requirement 5.3)
    val alpha by animateFloatAsState(
        targetValue = if (shouldShowPausedState) 0.7f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "pausedOpacity"
    )
    
    // Animate scale for active state to provide visual feedback
    val scale by animateFloatAsState(
        targetValue = if (isActive && gameState == GameState.RUNNING) 1.02f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "activeScale"
    )
    
    // Click animation effect
    val clickScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    
    // Trigger click animation when tapped
    suspend fun animateClick() {
        clickScale.animateTo(
            targetValue = 0.95f,
            animationSpec = tween(durationMillis = 100)
        )
        clickScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
    }
    
    // Get player color from theme, or red if this player lost
    val backgroundColor = when {
        gameState == GameState.GAME_OVER && winner != null && winner != player -> {
            // Show red for the losing player
            Color(0xFFD32F2F) // Material Red 700
        }
        else -> {
            // Use theme color for normal states from LocalGameColors
            when (player) {
                Player.PLAYER_ONE -> gameColors.player1Color
                Player.PLAYER_TWO -> gameColors.player2Color
            }
        }
    }
    
    // Use pre-calculated text color for contrast from LocalGameColors
    val textColor = when {
        gameState == GameState.GAME_OVER && winner != null && winner != player -> {
            // Use white text on red background for losing player
            Color.White
        }
        else -> {
            when (player) {
                Player.PLAYER_ONE -> gameColors.onPlayer1Color
                Player.PLAYER_TWO -> gameColors.onPlayer2Color
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale * clickScale.value)
            .background(backgroundColor)
            .alpha(alpha)
            .clickable(
                enabled = when (gameState) {
                    GameState.RUNNING -> isActive // Only active player's area is clickable when running
                    GameState.PAUSED -> true // Both areas clickable when paused (for resuming)
                    GameState.STOPPED -> true // Both areas clickable when stopped (for starting)
                    else -> false // No areas clickable when game over
                }
            ) {
                // Provide haptic feedback (Requirement 7.2)
                performHapticFeedback(context)
                // Trigger click animation
                coroutineScope.launch {
                    animateClick()
                }
                onPlayerTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatPlayerTime(timeInSeconds),
            fontSize = fontSize, // Responsive font size (Requirement 7.5)
            fontFamily = FontFamily.Monospace, // Monospaced font (Requirement 7.3)
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = if (player == Player.PLAYER_TWO) {
                Modifier.rotate(180f) // Rotate Player 2 display
            } else {
                Modifier
            }
        )
    }
}

/**
 * Formats time in seconds to MM:SS format
 */
private fun formatPlayerTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

/**
 * Determines if a color is light (for contrast calculation)
 */
private fun isLightColor(color: Color): Boolean {
    val red = color.red
    val green = color.green
    val blue = color.blue
    
    // Calculate luminance using standard formula
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}

/**
 * Performs haptic feedback for touch interactions (Requirement 7.2)
 */
private fun performHapticFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        }
    } catch (e: Exception) {
        // Haptic feedback is not critical, silently ignore errors
    }
}