package com.example.gameclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gameclock.ui.SettingsScreen
import com.example.gameclock.ui.ThemeViewModel
import com.example.gameclock.ui.theme.ChessClockTheme
import com.example.gameclock.ui.theme.Theme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current))
            val theme by themeViewModel.theme.collectAsState()

            ChessClockTheme(theme = theme) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "clock") {
                    composable("clock") { ChessClockScreen(onNavigateToSettings = { navController.navigate("settings") }) }
                    composable("settings") {
                        SettingsScreen(
                            selectedTheme = theme,
                            onThemeChange = { themeViewModel.setTheme(it) },
                            onDone = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessClockScreen(onNavigateToSettings: () -> Unit) {
    var player1TimeMs by remember { mutableStateOf(600_000L) }
    var player2TimeMs by remember { mutableStateOf(600_000L) }
    var isPlayer1sTurn by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf(0L) }

    val player1Weight by animateFloatAsState(
        targetValue = if (!isRunning) 1f else if (isPlayer1sTurn) 1.4f else 0.6f,
        animationSpec = tween(durationMillis = 300),
        label = "Player 1 Weight"
    )
    val player2Weight by animateFloatAsState(
        targetValue = if (!isRunning) 1f else if (isPlayer1sTurn) 0.6f else 1.4f,
        animationSpec = tween(durationMillis = 300),
        label = "Player 2 Weight"
    )

    LaunchedEffect(isRunning, isPlayer1sTurn) {
        while (isRunning) {
            val currentTime = System.currentTimeMillis()
            if (lastUpdateTime != 0L) {
                val delta = currentTime - lastUpdateTime
                if (isPlayer1sTurn) {
                    if (player1TimeMs > 0) player1TimeMs = maxOf(0, player1TimeMs - delta)
                } else {
                    if (player2TimeMs > 0) player2TimeMs = maxOf(0, player2TimeMs - delta)
                }
            }
            lastUpdateTime = currentTime
            delay(10)
        }
        lastUpdateTime = 0L
    }

    val colorScheme = MaterialTheme.colorScheme

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(player2Weight)
                    .fillMaxWidth()
            ) {
                PlayerTimer(
                    timeMs = player2TimeMs,
                    isActive = isRunning && !isPlayer1sTurn,
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            isPlayer1sTurn = true
                        } else if (isRunning && !isPlayer1sTurn) {
                            isPlayer1sTurn = true
                        }
                    },
                    backgroundColor = colorScheme.secondary,
                    textColor = colorScheme.onSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(player1Weight)
                    .fillMaxWidth()
            ) {
                PlayerTimer(
                    timeMs = player1TimeMs,
                    isActive = isRunning && isPlayer1sTurn,
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            isPlayer1sTurn = false
                        } else if (isRunning && isPlayer1sTurn) {
                            isPlayer1sTurn = false
                        }
                    },
                    backgroundColor = colorScheme.primary,
                    textColor = colorScheme.onPrimary
                )
            }
        }

        val fabOffsetY = (maxHeight * player2Weight / (player1Weight + player2Weight)) - 28.dp

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = fabOffsetY),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            if (!isRunning) {
                FloatingActionButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                }
            }

            FloatingActionButton(
                onClick = { isRunning = !isRunning }
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                )
            }

            if (!isRunning) {
                FloatingActionButton(onClick = { /* TODO: Change timer settings */ }) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Change Timer"
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerTimer(
    timeMs: Long,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = formatTime(timeMs),
                fontSize = 64.sp,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
fun GameClockPreview() {
    ChessClockTheme {
        ChessClockScreen(onNavigateToSettings = {})
    }
}