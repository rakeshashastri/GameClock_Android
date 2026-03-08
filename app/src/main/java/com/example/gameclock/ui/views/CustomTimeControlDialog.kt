package com.example.gameclock.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.DelayType
import com.example.gameclock.models.TimeControl

@Composable
fun CustomTimeControlDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSave: (TimeControl) -> Unit,
    onSaveDifferent: ((TimeControl, TimeControl) -> Unit)? = null,
    canSaveMoreCustomTimeControls: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        CustomTimeControlDialogContent(
            onDismiss = onDismiss,
            onSave = onSave,
            onSaveDifferent = onSaveDifferent,
            canSaveMoreCustomTimeControls = canSaveMoreCustomTimeControls,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimeControlDialogContent(
    onDismiss: () -> Unit,
    onSave: (TimeControl) -> Unit,
    onSaveDifferent: ((TimeControl, TimeControl) -> Unit)?,
    canSaveMoreCustomTimeControls: Boolean,
    modifier: Modifier = Modifier
) {
    // Player 1 (or shared) fields
    var minutesText by remember { mutableStateOf("") }
    var incrementText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var selectedDelayType by remember { mutableStateOf(DelayType.NONE) }

    // Asymmetric controls
    var sameForBothPlayers by remember { mutableStateOf(true) }
    var p2MinutesText by remember { mutableStateOf("") }
    var p2IncrementText by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var delayDropdownExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isValidInput = remember(minutesText, incrementText, nameText, sameForBothPlayers, p2MinutesText, p2IncrementText) {
        val minutes = minutesText.toIntOrNull()
        val increment = incrementText.toIntOrNull() ?: 0
        val p2Minutes = p2MinutesText.toIntOrNull()
        val p2Increment = p2IncrementText.toIntOrNull() ?: 0

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
            !sameForBothPlayers && (p2Minutes == null || p2Minutes !in 1..60) -> {
                errorMessage = "Player 2 minutes must be between 1 and 60"
                false
            }
            !sameForBothPlayers && p2Increment !in 0..60 -> {
                errorMessage = "Player 2 increment must be between 0 and 60 seconds"
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

    // Auto-determine delay type based on increment
    LaunchedEffect(incrementText, selectedDelayType) {
        val increment = incrementText.toIntOrNull() ?: 0
        if (increment > 0 && selectedDelayType == DelayType.NONE) {
            selectedDelayType = DelayType.FISCHER
        }
    }

    // Auto-generate name
    LaunchedEffect(minutesText, incrementText, selectedDelayType, sameForBothPlayers, p2MinutesText, p2IncrementText) {
        val minutes = minutesText.toIntOrNull()
        val increment = incrementText.toIntOrNull() ?: 0

        if (minutes != null && minutes > 0) {
            val delayLabel = when (selectedDelayType) {
                DelayType.BRONSTEIN -> " (Bronstein)"
                DelayType.SIMPLE_DELAY -> " (Delay)"
                else -> ""
            }
            val p1Part = if (increment > 0) "$minutes+$increment" else "${minutes}m"

            if (!sameForBothPlayers) {
                val p2Minutes = p2MinutesText.toIntOrNull()
                val p2Increment = p2IncrementText.toIntOrNull() ?: 0
                if (p2Minutes != null && p2Minutes > 0) {
                    val p2Part = if (p2Increment > 0) "$p2Minutes+$p2Increment" else "${p2Minutes}m"
                    nameText = "P1: $p1Part | P2: $p2Part$delayLabel"
                } else {
                    nameText = if (increment > 0) {
                        "$minutes min + $increment sec$delayLabel"
                    } else {
                        "$minutes min"
                    }
                }
            } else {
                nameText = if (increment > 0) {
                    "$minutes min + $increment sec$delayLabel"
                } else {
                    "$minutes min"
                }
            }
        }
    }

    val delayTypeLabels = mapOf(
        DelayType.NONE to "None",
        DelayType.FISCHER to "Fischer",
        DelayType.BRONSTEIN to "Bronstein",
        DelayType.SIMPLE_DELAY to "Simple Delay"
    )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // "Same for both players" toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Same for both players",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = sameForBothPlayers,
                        onCheckedChange = { sameForBothPlayers = it }
                    )
                }

                // Section label
                if (!sameForBothPlayers) {
                    Text(
                        text = "Player 1",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Player 1 Minutes input
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { newValue ->
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

                // Player 1 Increment input
                OutlinedTextField(
                    value = incrementText,
                    onValueChange = { newValue ->
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

                // Player 2 section (when asymmetric)
                if (!sameForBothPlayers) {
                    Divider()

                    Text(
                        text = "Player 2",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = p2MinutesText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                p2MinutesText = newValue
                                showError = false
                            }
                        },
                        label = { Text("Minutes") },
                        placeholder = { Text("1-60") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showError && !sameForBothPlayers && p2MinutesText.toIntOrNull() !in 1..60,
                        supportingText = {
                            if (showError && !sameForBothPlayers && p2MinutesText.toIntOrNull() !in 1..60) {
                                Text(
                                    text = "Enter minutes between 1 and 60",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = p2IncrementText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                p2IncrementText = newValue
                                showError = false
                            }
                        },
                        label = { Text("Increment (seconds)") },
                        placeholder = { Text("0-60 (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showError && !sameForBothPlayers && (p2IncrementText.toIntOrNull() ?: 0) !in 0..60,
                        supportingText = {
                            if (showError && !sameForBothPlayers && (p2IncrementText.toIntOrNull() ?: 0) !in 0..60) {
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

                    Divider()
                }

                // Delay Type dropdown
                ExposedDropdownMenuBox(
                    expanded = delayDropdownExpanded,
                    onExpandedChange = { delayDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = delayTypeLabels[selectedDelayType] ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Delay Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = delayDropdownExpanded) },
                        supportingText = {
                            Text(
                                text = when (selectedDelayType) {
                                    DelayType.NONE -> "No increment or delay"
                                    DelayType.FISCHER -> "Time added after each move"
                                    DelayType.BRONSTEIN -> "Time added, capped at time spent"
                                    DelayType.SIMPLE_DELAY -> "Delay before clock starts ticking"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = delayDropdownExpanded,
                        onDismissRequest = { delayDropdownExpanded = false }
                    ) {
                        DelayType.entries.forEach { delayType ->
                            DropdownMenuItem(
                                text = { Text(delayTypeLabels[delayType] ?: delayType.name) },
                                onClick = {
                                    selectedDelayType = delayType
                                    delayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Name input
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { newValue ->
                        if (newValue.length <= 30) {
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

                if (showError && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

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

                        if (!sameForBothPlayers && onSaveDifferent != null) {
                            val p2Minutes = p2MinutesText.toIntOrNull() ?: 0
                            val p2Increment = p2IncrementText.toIntOrNull() ?: 0

                            val p1TimeControl = TimeControl(
                                name = nameText.trim(),
                                timeInSeconds = minutes * 60L,
                                incrementInSeconds = increment.toLong(),
                                delayType = selectedDelayType
                            )
                            val p2TimeControl = TimeControl(
                                name = nameText.trim(),
                                timeInSeconds = p2Minutes * 60L,
                                incrementInSeconds = p2Increment.toLong(),
                                delayType = selectedDelayType
                            )
                            onSaveDifferent(p1TimeControl, p2TimeControl)
                            onDismiss()
                        } else {
                            val timeControl = TimeControl(
                                name = nameText.trim(),
                                timeInSeconds = minutes * 60L,
                                incrementInSeconds = increment.toLong(),
                                delayType = selectedDelayType
                            )
                            onSave(timeControl)
                            onDismiss()
                        }
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
