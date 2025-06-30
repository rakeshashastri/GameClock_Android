package com.example.gameclock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gameclock.ui.theme.Theme

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
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}