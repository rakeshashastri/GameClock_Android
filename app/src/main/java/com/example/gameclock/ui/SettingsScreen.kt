package com.example.gameclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.AppTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme

@Composable
fun ThemeSwatch(
    primary: Color,
    secondary: Color,
    selected: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(primary)
            .border(
                width = if (selected) 4.dp else 2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(size / 2)
                .clip(RoundedCornerShape(8.dp))
                .background(secondary)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun SettingsScreen(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onDone: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Theme")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            AppTheme.ALL_THEMES.forEach { theme ->
                ThemeSwatch(
                    primary = Color(theme.player1Color),
                    secondary = Color(theme.player2Color),
                    selected = selectedTheme.id == theme.id,
                    onClick = { onThemeChange(theme) }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FloatingActionButton(onClick = onDone) {
                Icon(imageVector = Icons.Filled.Done, contentDescription = "Done")
            }
        }
    }
}