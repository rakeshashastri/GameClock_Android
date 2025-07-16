
package com.example.gameclock.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CustomTimeControlView(
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column {
        Text("Custom Time Control")
        Button(onClick = { onSave("Custom") }) {
            Text("Save")
        }
        Button(onClick = onCancel) {
            Text("Cancel")
        }
    }
}
