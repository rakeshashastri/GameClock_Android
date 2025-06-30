package com.example.gameclock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class Theme {
    CLASSIC,
    MODERN,
    MINIMALIST
}

private val ClassicColorScheme = lightColorScheme(
    primary = ClassicDarkSlateGrey,
    secondary = ClassicLightGrey,
    onPrimary = ClassicTextOnDark,
    onSecondary = ClassicTextOnLight,
)

private val ModernColorScheme = lightColorScheme(
    primary = ModernDeepTeal,
    secondary = ModernWarmSand,
    onPrimary = ModernText,
    onSecondary = ModernText,
)

private val MinimalistColorScheme = lightColorScheme(
    primary = MinimalistMidnightBlue,
    secondary = MinimalistSilver,
    onPrimary = MinimalistTextOnDark,
    onSecondary = MinimalistTextOnLight,
)

@Composable
fun ChessClockTheme(
    theme: Theme = Theme.CLASSIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        Theme.CLASSIC -> ClassicColorScheme
        Theme.MODERN -> ModernColorScheme
        Theme.MINIMALIST -> MinimalistColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}