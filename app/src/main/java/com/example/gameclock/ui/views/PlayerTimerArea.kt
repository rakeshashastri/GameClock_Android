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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.gameclock.models.TimeControl
import com.example.gameclock.ui.theme.LocalGameColors

@Composable
fun PlayerTimerArea(
    player: Player,
    timeInMs: Long,
    gameState: GameState,
    activePlayer: Player?,
    isActive: Boolean,
    isPaused: Boolean,
    winner: Player?,
    theme: AppTheme,
    onPlayerTap: () -> Unit,
    modifier: Modifier = Modifier,
    moveCount: Int = 0,
    stageIndex: Int = 0,
    totalStages: Int = 1,
    delayRemainingMs: Long = 0L,
    isLowTime: Boolean = false,
    lowTimeWarningEnabled: Boolean = true,
    // Backward compat: if timeInSeconds is passed instead
    timeInSeconds: Long = -1L
) {
    val effectiveTimeMs = if (timeInSeconds >= 0) timeInSeconds * 1000L else timeInMs

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val gameColors = LocalGameColors.current

    val screenWidth = configuration.screenWidthDp
    val fontSize = when {
        screenWidth < 360 -> 56.sp
        screenWidth < 480 -> 64.sp
        else -> 72.sp
    }

    val shouldShowPausedState = isPaused && gameState == GameState.PAUSED

    val alpha by animateFloatAsState(
        targetValue = if (shouldShowPausedState) 0.7f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "pausedOpacity"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive && gameState == GameState.RUNNING) 1.01f else 1.0f,
        animationSpec = tween(durationMillis = 250),
        label = "activeScale"
    )

    val clickScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun animateClick() {
        clickScale.animateTo(
            targetValue = 0.98f,
            animationSpec = tween(durationMillis = 80)
        )
        clickScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 120)
        )
    }

    // Low time flashing animation
    val showLowTimeWarning = isLowTime && lowTimeWarningEnabled && isActive && gameState == GameState.RUNNING
    val infiniteTransition = rememberInfiniteTransition(label = "lowTimeFlash")
    val lowTimeAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (showLowTimeWarning) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lowTimeAlpha"
    )

    val backgroundColor = when {
        gameState == GameState.GAME_OVER && winner != null && winner != player -> {
            Color(0xFFD32F2F)
        }
        else -> {
            when (player) {
                Player.PLAYER_ONE -> gameColors.player1Color
                Player.PLAYER_TWO -> gameColors.player2Color
            }
        }
    }

    val textColor = when {
        gameState == GameState.GAME_OVER && winner != null && winner != player -> {
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
            .background(backgroundColor)
            .alpha(alpha)
            .clickable(
                enabled = when (gameState) {
                    GameState.RUNNING -> isActive
                    GameState.PAUSED -> true
                    GameState.STOPPED -> true
                    else -> false
                }
            ) {
                performHapticFeedback(context)
                coroutineScope.launch {
                    animateClick()
                }
                onPlayerTap()
            },
        contentAlignment = Alignment.Center
    ) {
        // Low time warning flash overlay
        if (showLowTimeWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = lowTimeAlpha))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = (if (player == Player.PLAYER_TWO) Modifier.rotate(180f) else Modifier)
                .scale(scale * clickScale.value)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // For Player 2 (rotated 180°), put info above timer so it appears below after rotation
            if (player == Player.PLAYER_TWO) {
                MovesInfo(gameState, moveCount, totalStages, stageIndex, isActive, delayRemainingMs, textColor)
            }

            // Main timer display
            Text(
                text = formatPlayerTimeMs(effectiveTimeMs),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // For Player 1 (not rotated), put info below timer
            if (player == Player.PLAYER_ONE) {
                MovesInfo(gameState, moveCount, totalStages, stageIndex, isActive, delayRemainingMs, textColor)
            }
        }
    }
}

/**
 * Formats time in milliseconds with intelligent formatting:
 * - H:MM:SS when >= 1 hour
 * - MM:SS when >= 20 seconds
 * - MM:SS.T (with tenths) when < 20 seconds
 */
private fun formatPlayerTimeMs(ms: Long): String {
    val totalSeconds = ms / 1000L
    val tenths = (ms % 1000L) / 100L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return when {
        hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
        ms < 20_000L -> "%02d:%02d.%d".format(minutes, seconds, tenths)
        else -> "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun MovesInfo(
    gameState: GameState,
    moveCount: Int,
    totalStages: Int,
    stageIndex: Int,
    isActive: Boolean,
    delayRemainingMs: Long,
    textColor: Color
) {
    if (totalStages > 1) {
        Text(
            text = "Stage ${stageIndex + 1}/$totalStages",
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
    }

    if (gameState in listOf(GameState.RUNNING, GameState.PAUSED) && moveCount > 0) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Moves: $moveCount",
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }

    if (isActive && delayRemainingMs > 0 && gameState == GameState.RUNNING) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Delay: %.1fs".format(delayRemainingMs / 1000.0),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

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
        // Haptic feedback is not critical
    }
}
