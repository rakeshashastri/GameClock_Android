package com.example.gameclock.ui.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.GameState
import com.example.gameclock.models.GameUiState
import com.example.gameclock.models.Player
import com.example.gameclock.models.TimeControl

/**
 * GameScreen composable that implements the main game interface with two PlayerTimerArea
 * components, a ControlButtonsOverlay in the center position, and integrated bottom sheets
 * and dialogs for settings and time control management.
 * 
 * Requirements addressed:
 * - 1.1: Display two equal timer areas for Player 1 and Player 2
 * - 2.1: Connect TimeControlBottomSheet to game screen
 * - 2.3: Integrate CustomTimeControlDialog with time control flow
 * - 4.1: Connect SettingsBottomSheet to game screen
 * - 7.1: Display in portrait orientation only
 * - 10.1: Use Material Design components and styling
 * - 10.4: Implement proper modal presentation patterns
 * - 10.5: Follow Material Design color system guidelines
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameUiState: GameUiState,
    currentTheme: AppTheme,
    availableThemes: List<AppTheme>,
    onPlayerTap: (Player) -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onTimeControlSelected: (TimeControl) -> Unit,
    onCustomTimeControlSave: (TimeControl) -> Unit,
    onCustomTimeControlDelete: (TimeControl) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Modal state management (Requirement 10.4: Proper modal presentation patterns)
    var showTimeControlBottomSheet by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showCustomTimeControlDialog by remember { mutableStateOf(false) }
    
    val timeControlSheetState = rememberModalBottomSheetState()
    
    // Calculate heights based on active player state (Requirement 5.2)
    val (player1Height, player2Height) = calculatePlayerHeights(
        gameUiState = gameUiState,
        screenHeight = screenHeight
    )
    
    // Animate height transitions for smooth area changes
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Player 2 Timer Area (Top - rotated 180°)
                PlayerTimerArea(
                    player = Player.PLAYER_TWO,
                    timeInSeconds = gameUiState.player2Time,
                    gameState = gameUiState.gameState,
                    activePlayer = gameUiState.activePlayer,
                    isActive = gameUiState.activePlayer == Player.PLAYER_TWO,
                    isPaused = gameUiState.gameState == GameState.PAUSED,
                    theme = currentTheme,
                    onPlayerTap = { onPlayerTap(Player.PLAYER_TWO) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedPlayer2Height)
                )
                
                // Player 1 Timer Area (Bottom)
                PlayerTimerArea(
                    player = Player.PLAYER_ONE,
                    timeInSeconds = gameUiState.player1Time,
                    gameState = gameUiState.gameState,
                    activePlayer = gameUiState.activePlayer,
                    isActive = gameUiState.activePlayer == Player.PLAYER_ONE,
                    isPaused = gameUiState.gameState == GameState.PAUSED,
                    theme = currentTheme,
                    onPlayerTap = { onPlayerTap(Player.PLAYER_ONE) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedPlayer1Height)
                )
            }
            
            // Control Buttons positioned at the border between play areas
            ControlButtonsOverlay(
                gameState = gameUiState.gameState,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onResetClick = onResetClick,
                onSettingsClick = { showSettingsBottomSheet = true },
                onTimeControlClick = { showTimeControlBottomSheet = true },
                borderPosition = animatedPlayer2Height, // Position at the animated border between areas
                modifier = Modifier.fillMaxSize()
            )
            
            // Winner Display Overlay (Requirement 1.4, 5.4, 8.4)
            gameUiState.winner?.let { winner ->
                if (gameUiState.gameState == GameState.GAME_OVER) {
                    WinnerDisplayOverlay(
                        winner = winner,
                        theme = currentTheme,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Time Control Bottom Sheet (Requirement 2.1: Connect TimeControlBottomSheet to game screen)
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
            onDismiss = { showTimeControlBottomSheet = false }
        )
        
        // Settings Bottom Sheet (Requirement 4.1: Connect SettingsBottomSheet to game screen)
        if (showSettingsBottomSheet) {
            SettingsBottomSheet(
                currentTheme = currentTheme,
                availableThemes = availableThemes,
                onThemeSelected = onThemeSelected,
                onDismiss = { showSettingsBottomSheet = false }
            )
        }
        
        // Custom Time Control Dialog (Requirement 2.3: Integrate CustomTimeControlDialog with time control flow)
        CustomTimeControlDialog(
            isVisible = showCustomTimeControlDialog,
            onDismiss = { showCustomTimeControlDialog = false },
            onSave = { timeControl ->
                onCustomTimeControlSave(timeControl)
                showCustomTimeControlDialog = false
            },
            canSaveMoreCustomTimeControls = gameUiState.customTimeControls.size < 5
        )
    }
}

/**
 * Calculates the heights for player timer areas based on game state and active player.
 * 
 * Requirements addressed:
 * - 5.1: Equal size display when no timer is active
 * - 5.2: Expand active player area to 70% of screen height
 */
private fun calculatePlayerHeights(
    gameUiState: GameUiState,
    screenHeight: androidx.compose.ui.unit.Dp
): Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp> {
    // Use full screen height - timer areas consume entire screen
    val availableHeight = screenHeight
    
    return when {
        // When game is running or paused and there's an active player, maintain the expanded area
        (gameUiState.gameState == GameState.RUNNING || gameUiState.gameState == GameState.PAUSED) && gameUiState.activePlayer != null -> {
            val activeHeight = availableHeight * 0.7f
            val inactiveHeight = availableHeight * 0.3f
            
            when (gameUiState.activePlayer) {
                Player.PLAYER_ONE -> Pair(activeHeight, inactiveHeight)
                Player.PLAYER_TWO -> Pair(inactiveHeight, activeHeight)
                null -> Pair(availableHeight / 2, availableHeight / 2) // Fallback
            }
        }
        
        // Equal heights for stopped and game over states
        else -> {
            val equalHeight = availableHeight / 2
            Pair(equalHeight, equalHeight)
        }
    }
}