
package com.example.gameclock.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.AppTheme

@Composable
fun SettingsScreen(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onDone: () -> Unit
) {
    Column {
        AppTheme.ALL_THEMES.forEach { theme ->
            Row(modifier = Modifier.clickable { onThemeChange(theme) }) {
                Text(text = theme.name)
                Spacer(modifier = Modifier.width(8.dp))
                if (theme.id == selectedTheme.id) {
                    Text(text = "✓")
                }
            }
        }
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}
