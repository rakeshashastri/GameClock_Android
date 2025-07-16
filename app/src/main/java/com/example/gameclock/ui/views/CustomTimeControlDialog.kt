package com.example.gameclock.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.TimeControl

@Composable
fun CustomTimeControlDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSave: (TimeControl) -> Unit,
    canSaveMoreCustomTimeControls: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        CustomTimeControlDialogContent(
            onDismiss = onDismiss,
            onSave = onSave,
            canSaveMoreCustomTimeControls = canSaveMoreCustomTimeControls,
            modifier = modifier
        )
    }
}

@Composable
private fun CustomTimeControlDialogContent(
    onDismiss: () -> Unit,
    onSave: (TimeControl) -> Unit,
    canSaveMoreCustomTimeControls: Boolean,
    modifier: Modifier = Modifier
) {
    var minutesText by remember { mutableStateOf("") }
    var incrementText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus the minutes field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Validation logic
    val isValidInput = remember(minutesText, incrementText, nameText) {
        val minutes = minutesText.toIntOrNull()
        val increment = incrementText.toIntOrNull() ?: 0
        
        when {
            nameText.isBlank() -> {
                errorMessage = "Name cannot be empty"
                false
            }
            minutes == null -> {
                errorMessage = "Minutes must be a valid number"
                false
            }
            minutes !in 1..60 -> {
                errorMessage = "Minutes must be between 1 and 60"
                false
            }
            increment !in 0..60 -> {
                errorMessage = "Increment must be between 0 and 60 seconds"
                false
            }
            !canSaveMoreCustomTimeControls -> {
                errorMessage = "Maximum of 5 custom time controls allowed"
                false
            }
            else -> {
                errorMessage = ""
                true
            }
        }
    }
    
    // Auto-generate name based on time and increment
    LaunchedEffect(minutesText, incrementText) {
        val minutes = minutesText.toIntOrNull()
        val increment = incrementText.toIntOrNull() ?: 0
        
        if (minutes != null && minutes > 0) {
            nameText = if (increment > 0) {
                "$minutes min + $increment sec"
            } else {
                "$minutes min"
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Custom Time Control",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Minutes input
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { newValue ->
                        // Only allow digits and limit to 2 characters
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            minutesText = newValue
                            showError = false
                        }
                    },
                    label = { Text("Minutes") },
                    placeholder = { Text("1-60") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && minutesText.toIntOrNull() !in 1..60,
                    supportingText = {
                        if (showError && minutesText.toIntOrNull() !in 1..60) {
                            Text(
                                text = "Enter minutes between 1 and 60",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                
                // Increment input
                OutlinedTextField(
                    value = incrementText,
                    onValueChange = { newValue ->
                        // Only allow digits and limit to 2 characters
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            incrementText = newValue
                            showError = false
                        }
                    },
                    label = { Text("Increment (seconds)") },
                    placeholder = { Text("0-60 (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && (incrementText.toIntOrNull() ?: 0) !in 0..60,
                    supportingText = {
                        if (showError && (incrementText.toIntOrNull() ?: 0) !in 0..60) {
                            Text(
                                text = "Enter increment between 0 and 60 seconds",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Time added after each move (leave empty for 0)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Name input (auto-generated but editable)
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { newValue ->
                        if (newValue.length <= 30) { // Reasonable limit for name length
                            nameText = newValue
                            showError = false
                        }
                    },
                    label = { Text("Name") },
                    placeholder = { Text("Custom name") },
                    isError = showError && nameText.isBlank(),
                    supportingText = {
                        if (showError && nameText.isBlank()) {
                            Text(
                                text = "Name cannot be empty",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Auto-generated, but you can customize it",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Error message display
                if (showError && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Limit warning
                if (!canSaveMoreCustomTimeControls) {
                    Text(
                        text = "You have reached the maximum of 5 custom time controls. Delete some to create new ones.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValidInput) {
                        val minutes = minutesText.toIntOrNull() ?: 0
                        val increment = incrementText.toIntOrNull() ?: 0
                        val timeControl = TimeControl(
                            name = nameText.trim(),
                            timeInSeconds = minutes * 60L,
                            incrementInSeconds = increment.toLong()
                        )
                        onSave(timeControl)
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
                enabled = canSaveMoreCustomTimeControls
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}