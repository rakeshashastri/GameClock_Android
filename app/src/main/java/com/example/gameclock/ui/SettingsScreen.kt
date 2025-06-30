package com.example.gameclock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gameclock.ui.theme.Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done

@Composable
fun SettingsScreen(
    selectedTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onDone: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Theme")
        Theme.values().forEach { theme ->
            Row {
                RadioButton(
                    selected = theme == selectedTheme,
                    onClick = { onThemeChange(theme) }
                )
                Text(text = theme.name, modifier = Modifier.padding(start = 8.dp))
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