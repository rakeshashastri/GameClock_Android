package com.example.gameclock.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.GameState
import kotlin.math.max
import kotlin.math.min

/**
 * High-contrast color scheme for floating action buttons that provides excellent visibility
 * against various background colors. Uses dark charcoal background with white icons
 * for optimal contrast and accessibility.
 */
private val ButtonBackgroundColor = Color(0xFF2D3748) // Dark charcoal/slate
private val ButtonIconColor = Color.White

/**
 * ControlButtonsOverlay composable that displays floating action buttons for game controls
 * positioned at the center of the screen with conditional visibility and smooth transitions.
 * 
 * Requirements addressed:
 * - 1.2: Display play button to start game
 * - 1.5: Display pause button during active gameplay
 * - 7.4: Proper touch target sizing for accessibility (minimum 48dp)
 * - 8.1: Display reset button when game is paused
 * - 10.3: Use Material Design button styles and elevation
 */
@Composable
fun ControlButtonsOverlay(
    gameState: GameState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTimeControlClick: () -> Unit,
    borderPosition: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Position buttons at the exact border between the two play areas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = borderPosition - 36.dp), // Offset by half the largest button height (72dp / 2)
            contentAlignment = Alignment.TopCenter
        ) {
            when (gameState) {
                GameState.STOPPED -> {
                    // Show play button, settings, and time control when stopped
                    StoppedStateButtons(
                        onPlayClick = onPlayClick,
                        onSettingsClick = onSettingsClick,
                        onTimeControlClick = onTimeControlClick
                    )
                }
                
                GameState.RUNNING -> {
                    // Show only pause button when running (Requirement 1.5)
                    RunningStateButtons(
                        onPauseClick = onPauseClick
                    )
                }
                
                GameState.PAUSED -> {
                    // Show play, reset, and settings when paused (Requirement 8.1)
                    PausedStateButtons(
                        onPlayClick = onPlayClick,
                        onResetClick = onResetClick,
                        onSettingsClick = onSettingsClick
                    )
                }
                
                GameState.GAME_OVER -> {
                    // Show reset and settings when game is over
                    GameOverStateButtons(
                        onResetClick = onResetClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}

/**
 * Buttons displayed when game is stopped
 */
@Composable
private fun StoppedStateButtons(
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTimeControlClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(56.dp) // Standard FAB size (Requirement 7.4)
                    .semantics { contentDescription = "Open settings" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ButtonIconColor
                )
            }
        }
        
        // Main play button (Requirement 1.2)
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300, delayMillis = 100)) + fadeIn(animationSpec = tween(300, delayMillis = 100)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(72.dp) // Large primary button
                    .semantics { contentDescription = "Start game" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = ButtonIconColor
                )
            }
        }
        
        // Time control button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300, delayMillis = 200)) + fadeIn(animationSpec = tween(300, delayMillis = 200)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onTimeControlClick,
                modifier = Modifier
                    .size(56.dp) // Standard FAB size (Requirement 7.4)
                    .semantics { contentDescription = "Select time control" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ButtonIconColor
                )
            }
        }
    }
}

/**
 * Buttons displayed when game is running
 */
@Composable
private fun RunningStateButtons(
    onPauseClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        FloatingActionButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(64.dp) // Medium size for pause button
                .semantics { contentDescription = "Pause game" },
            containerColor = ButtonBackgroundColor,
            contentColor = ButtonIconColor
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = ButtonIconColor
            )
        }
    }
}

/**
 * Buttons displayed when game is paused
 */
@Composable
private fun PausedStateButtons(
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset button (Requirement 8.1)
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onResetClick,
                modifier = Modifier
                    .size(56.dp) // Standard FAB size (Requirement 7.4)
                    .semantics { contentDescription = "Reset game" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = ButtonIconColor
                )
            }
        }
        
        // Resume button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300, delayMillis = 100)) + fadeIn(animationSpec = tween(300, delayMillis = 100)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(72.dp) // Large primary button
                    .semantics { contentDescription = "Resume game" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = ButtonIconColor
                )
            }
        }
        
        // Settings button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300, delayMillis = 200)) + fadeIn(animationSpec = tween(300, delayMillis = 200)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(56.dp) // Standard FAB size (Requirement 7.4)
                    .semantics { contentDescription = "Open settings" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ButtonIconColor
                )
            }
        }
    }
}

/**
 * Buttons displayed when game is over
 */
@Composable
private fun GameOverStateButtons(
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        // Reset button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onResetClick,
                modifier = Modifier
                    .size(64.dp) // Medium size for reset button
                    .semantics { contentDescription = "Reset game" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = ButtonIconColor
                )
            }
        }
        
        // Settings button
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(300, delayMillis = 100)) + fadeIn(animationSpec = tween(300, delayMillis = 100)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            FloatingActionButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(56.dp) // Standard FAB size (Requirement 7.4)
                    .semantics { contentDescription = "Open settings" },
                containerColor = ButtonBackgroundColor,
                contentColor = ButtonIconColor
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ButtonIconColor
                )
            }
        }
    }
}