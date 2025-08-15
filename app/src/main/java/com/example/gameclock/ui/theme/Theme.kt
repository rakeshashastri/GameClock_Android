package com.example.gameclock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.gameclock.models.AppTheme

// Custom composition local for game-specific colors
val LocalGameColors = compositionLocalOf { GameColors() }

data class GameColors(
    val player1Color: Color = Color(0xFF1A1A1A),
    val player2Color: Color = Color(0xFFE5E5E5),
    val onPlayer1Color: Color = Color.White,
    val onPlayer2Color: Color = Color.Black
)

private fun createGameColors(appTheme: AppTheme): GameColors {
    val player1Color = Color(appTheme.player1Color)
    val player2Color = Color(appTheme.player2Color)
    
    // Calculate appropriate text colors based on background luminance for accessibility
    val onPlayer1Color = if (player1Color.luminance() > 0.5f) Color.Black else Color.White
    val onPlayer2Color = if (player2Color.luminance() > 0.5f) Color.Black else Color.White
    
    return GameColors(
        player1Color = player1Color,
        player2Color = player2Color,
        onPlayer1Color = onPlayer1Color,
        onPlayer2Color = onPlayer2Color
    )
}

private fun createMaterialColorScheme(
    appTheme: AppTheme,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    context: android.content.Context
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme || appTheme.isDark -> {
            darkColorScheme(
                primary = Color(appTheme.player1Color),
                secondary = Color(appTheme.player2Color),
                surface = Color(0xFF121212),
                background = Color(0xFF121212),
                onSurface = Color.White,
                onBackground = Color.White
            )
        }
        else -> {
            lightColorScheme(
                primary = Color(appTheme.player1Color),
                secondary = Color(appTheme.player2Color),
                surface = Color(0xFFFFFBFE),
                background = Color(0xFFFFFBFE),
                onSurface = Color.Black,
                onBackground = Color.Black
            )
        }
    }
}

@Composable
fun ChessClockTheme(
    theme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val gameColors = createGameColors(theme)
    val colorScheme = createMaterialColorScheme(
        appTheme = theme,
        darkTheme = darkTheme,
        dynamicColor = false, // Disable dynamic colors for chess clock to maintain theme consistency
        context = context
    )

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun GameClockTheme(
    theme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Allow dynamic colors but default to false for consistency
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val gameColors = createGameColors(theme)
    val colorScheme = createMaterialColorScheme(
        appTheme = theme,
        darkTheme = darkTheme || theme.isDark,
        dynamicColor = dynamicColor,
        context = context
    )

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}