
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
import com.example.gameclock.models.TimeControl

@Composable
fun TimeControlView(
    onTimeControlSelected: (TimeControl) -> Unit,
    onDone: () -> Unit,
    onNavigateToCustomTimeControl: () -> Unit
) {
    val timeControls = listOf(
        TimeControl(name = "5 min", timeInSeconds = 300, incrementInSeconds = 0),
        TimeControl(name = "10 min", timeInSeconds = 600, incrementInSeconds = 0),
        TimeControl(name = "15 min", timeInSeconds = 900, incrementInSeconds = 0)
    )

    Column {
        timeControls.forEach { timeControl ->
            Row(modifier = Modifier.clickable { onTimeControlSelected(timeControl) }) {
                Text(text = timeControl.name)
            }
        }
        Button(onClick = onNavigateToCustomTimeControl) {
            Text("Create Custom")
        }
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}
