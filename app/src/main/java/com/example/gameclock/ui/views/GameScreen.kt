package com.example.gameclock.ui.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.GameState
import com.example.gameclock.models.GameUiState
import com.example.gameclock.models.Player
import com.example.gameclock.models.TimeControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameUiState: GameUiState,
    currentTheme: AppTheme,
    availableThemes: List<AppTheme>,
    isPremium: Boolean = true,
    subscriptionPrice: String = "$4.99/month",
    purchaseInProgress: Boolean = false,
    onSubscribe: () -> Unit = {},
    onRestore: () -> Unit = {},
    onPlayerTap: (Player) -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onTimeControlSelected: (TimeControl) -> Unit,
    onCustomTimeControlSave: (TimeControl) -> Unit,
    onCustomTimeControlSaveDifferent: ((TimeControl, TimeControl) -> Unit)? = null,
    onCustomTimeControlDelete: (TimeControl) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onLowTimeWarningChanged: (Boolean) -> Unit = {},
    onLowTimeThresholdChanged: (Long) -> Unit = {},
    onTapSoundChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showTimeControlBottomSheet by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showCustomTimeControlDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    val timeControlSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Game Clock - ${
                    when (gameUiState.gameState) {
                        GameState.STOPPED -> "Ready to start"
                        GameState.RUNNING -> "Game in progress"
                        GameState.PAUSED -> "Game paused"
                        GameState.GAME_OVER -> "Game over"
                    }
                }"
            },
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val actualScreenHeight = maxHeight

            val (player1Height, player2Height) = calculatePlayerHeights(
                gameUiState = gameUiState,
                screenHeight = actualScreenHeight
            )

            val animatedPlayer1Height by animateDpAsState(
                targetValue = player1Height,
                animationSpec = tween(durationMillis = 300),
                label = "player1Height"
            )

            val animatedPlayer2Height by animateDpAsState(
                targetValue = player2Height,
                animationSpec = tween(durationMillis = 300),
                label = "player2Height"
            )

            val p2Tc = gameUiState.player2TimeControl
            val p1Tc = gameUiState.player1TimeControl

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Player 2 Timer Area (Top - rotated 180 degrees)
                PlayerTimerArea(
                    player = Player.PLAYER_TWO,
                    timeInMs = gameUiState.player2TimeMs,
                    gameState = gameUiState.gameState,
                    activePlayer = gameUiState.activePlayer,
                    isActive = gameUiState.activePlayer == Player.PLAYER_TWO,
                    isPaused = gameUiState.gameState == GameState.PAUSED,
                    winner = gameUiState.winner,
                    theme = currentTheme,
                    onPlayerTap = { onPlayerTap(Player.PLAYER_TWO) },
                    moveCount = gameUiState.player2Moves,
                    stageIndex = gameUiState.player2StageIndex,
                    totalStages = p2Tc.effectiveStages.size,
                    delayRemainingMs = if (gameUiState.activePlayer == Player.PLAYER_TWO) gameUiState.delayRemainingMs else 0L,
                    isLowTime = gameUiState.isLowTime2,
                    lowTimeWarningEnabled = gameUiState.lowTimeWarningEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedPlayer2Height)
                )

                // Player 1 Timer Area (Bottom)
                PlayerTimerArea(
                    player = Player.PLAYER_ONE,
                    timeInMs = gameUiState.player1TimeMs,
                    gameState = gameUiState.gameState,
                    activePlayer = gameUiState.activePlayer,
                    isActive = gameUiState.activePlayer == Player.PLAYER_ONE,
                    isPaused = gameUiState.gameState == GameState.PAUSED,
                    winner = gameUiState.winner,
                    theme = currentTheme,
                    onPlayerTap = { onPlayerTap(Player.PLAYER_ONE) },
                    moveCount = gameUiState.player1Moves,
                    stageIndex = gameUiState.player1StageIndex,
                    totalStages = p1Tc.effectiveStages.size,
                    delayRemainingMs = if (gameUiState.activePlayer == Player.PLAYER_ONE) gameUiState.delayRemainingMs else 0L,
                    isLowTime = gameUiState.isLowTime1,
                    lowTimeWarningEnabled = gameUiState.lowTimeWarningEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedPlayer1Height)
                )
            }

            // Control Buttons
            ControlButtonsOverlay(
                gameState = gameUiState.gameState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onResetClick = { showResetConfirmation = true },
                onSettingsClick = { showSettingsBottomSheet = true },
                onTimeControlClick = { showTimeControlBottomSheet = true },
                borderPosition = animatedPlayer2Height,
                modifier = Modifier.fillMaxSize()
            )

            // Winner overlay
            if (gameUiState.gameState == GameState.GAME_OVER && gameUiState.winner != null) {
                WinnerDisplayOverlay(
                    winner = gameUiState.winner,
                    theme = currentTheme
                )
            }
        }

        // Time Control Bottom Sheet
        TimeControlBottomSheet(
            isVisible = showTimeControlBottomSheet,
            sheetState = timeControlSheetState,
            recentTimeControls = gameUiState.recentTimeControls,
            customTimeControls = gameUiState.customTimeControls,
            onTimeControlSelected = onTimeControlSelected,
            onCustomTimeControlDelete = onCustomTimeControlDelete,
            onCreateCustomTimeControl = {
                showTimeControlBottomSheet = false
                showCustomTimeControlDialog = true
            },
            onDismiss = { showTimeControlBottomSheet = false },
            isPremium = isPremium,
            onShowPaywall = { showPaywall = true }
        )

        // Settings Bottom Sheet
        if (showSettingsBottomSheet) {
            SettingsBottomSheet(
                currentTheme = currentTheme,
                availableThemes = availableThemes,
                onThemeSelected = onThemeSelected,
                lowTimeWarningEnabled = gameUiState.lowTimeWarningEnabled,
                onLowTimeWarningChanged = onLowTimeWarningChanged,
                lowTimeThresholdMs = gameUiState.lowTimeThresholdMs,
                onLowTimeThresholdChanged = onLowTimeThresholdChanged,
                tapSoundEnabled = gameUiState.tapSoundEnabled,
                onTapSoundChanged = onTapSoundChanged,
                onDismiss = { showSettingsBottomSheet = false },
                isPremium = isPremium,
                onShowPaywall = { showPaywall = true }
            )
        }

        // Reset Confirmation Dialog
        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                title = { Text("Reset Game?") },
                text = { Text("Time and moves will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        showResetConfirmation = false
                        onResetClick()
                    }) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Time Control Dialog
        CustomTimeControlDialog(
            isVisible = showCustomTimeControlDialog,
            onDismiss = { showCustomTimeControlDialog = false },
            onSave = { timeControl ->
                onCustomTimeControlSave(timeControl)
                showCustomTimeControlDialog = false
            },
            onSaveDifferent = if (onCustomTimeControlSaveDifferent != null) {
                { p1, p2 ->
                    onCustomTimeControlSaveDifferent(p1, p2)
                    showCustomTimeControlDialog = false
                }
            } else null,
            canSaveMoreCustomTimeControls = gameUiState.customTimeControls.size < 5
        )

        // Paywall Dialog
        if (showPaywall) {
            PaywallDialog(
                subscriptionPrice = subscriptionPrice,
                purchaseInProgress = purchaseInProgress,
                onSubscribe = onSubscribe,
                onRestore = onRestore,
                onDismiss = { showPaywall = false }
            )
        }
    }
}

private fun calculatePlayerHeights(
    gameUiState: GameUiState,
    screenHeight: androidx.compose.ui.unit.Dp
): Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp> {
    val availableHeight = screenHeight

    return when {
        (gameUiState.gameState == GameState.RUNNING || gameUiState.gameState == GameState.PAUSED) && gameUiState.activePlayer != null -> {
            val activeHeight = availableHeight * 0.7f
            val inactiveHeight = availableHeight * 0.3f

            when (gameUiState.activePlayer) {
                Player.PLAYER_ONE -> Pair(activeHeight, inactiveHeight)
                Player.PLAYER_TWO -> Pair(inactiveHeight, activeHeight)
                null -> Pair(availableHeight / 2, availableHeight / 2)
            }
        }

        else -> {
            val equalHeight = availableHeight / 2
            Pair(equalHeight, equalHeight)
        }
    }
}
