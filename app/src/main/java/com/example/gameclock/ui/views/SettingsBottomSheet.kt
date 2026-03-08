package com.example.gameclock.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentTheme: AppTheme,
    availableThemes: List<AppTheme>,
    onThemeSelected: (AppTheme) -> Unit,
    lowTimeWarningEnabled: Boolean = true,
    onLowTimeWarningChanged: (Boolean) -> Unit = {},
    lowTimeThresholdMs: Long = 30_000L,
    onLowTimeThresholdChanged: (Long) -> Unit = {},
    tapSoundEnabled: Boolean = true,
    onTapSoundChanged: (Boolean) -> Unit = {},
    onDismiss: () -> Unit,
    isPremium: Boolean = true,
    onShowPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("settings_bottom_sheet")
    ) {
        SettingsBottomSheetContent(
            currentTheme = currentTheme,
            availableThemes = availableThemes,
            onThemeSelected = onThemeSelected,
            lowTimeWarningEnabled = lowTimeWarningEnabled,
            onLowTimeWarningChanged = onLowTimeWarningChanged,
            lowTimeThresholdMs = lowTimeThresholdMs,
            onLowTimeThresholdChanged = onLowTimeThresholdChanged,
            tapSoundEnabled = tapSoundEnabled,
            onTapSoundChanged = onTapSoundChanged,
            isPremium = isPremium,
            onShowPaywall = onShowPaywall
        )
    }
}

@Composable
private fun SettingsBottomSheetContent(
    currentTheme: AppTheme,
    availableThemes: List<AppTheme>,
    onThemeSelected: (AppTheme) -> Unit,
    lowTimeWarningEnabled: Boolean,
    onLowTimeWarningChanged: (Boolean) -> Unit,
    lowTimeThresholdMs: Long = 30_000L,
    onLowTimeThresholdChanged: (Long) -> Unit = {},
    tapSoundEnabled: Boolean = true,
    onTapSoundChanged: (Boolean) -> Unit = {},
    isPremium: Boolean = true,
    onShowPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("settings_content")
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Theme Selection Section
        ThemeSelectionSection(
            currentTheme = currentTheme,
            availableThemes = availableThemes,
            onThemeSelected = onThemeSelected,
            isPremium = isPremium,
            onShowPaywall = onShowPaywall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Game Options Section
        Text(
            text = "Game",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Low Time Warning Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Low Time Warning",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Flash red when time is low",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = lowTimeWarningEnabled,
                onCheckedChange = onLowTimeWarningChanged
            )
        }

        // Warning Threshold Picker (only when warning is enabled)
        if (lowTimeWarningEnabled) {
            val thresholdOptions = listOf(
                10_000L to "10 seconds",
                20_000L to "20 seconds",
                30_000L to "30 seconds",
                60_000L to "60 seconds"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Warning Threshold",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                var expanded by remember { mutableStateOf(false) }

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = thresholdOptions.firstOrNull { it.first == lowTimeThresholdMs }?.second ?: "30 seconds"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        thresholdOptions.forEach { (ms, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onLowTimeThresholdChanged(ms)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tap Sound Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tap Sound",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Play a click sound when tapping the clock",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = tapSoundEnabled,
                onCheckedChange = onTapSoundChanged
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ThemeSelectionSection(
    currentTheme: AppTheme,
    availableThemes: List<AppTheme>,
    onThemeSelected: (AppTheme) -> Unit,
    isPremium: Boolean = true,
    onShowPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("theme_list")
        ) {
            items(availableThemes) { theme ->
                val isDefault = theme.id == "default"
                val isLocked = !isPremium && !isDefault
                ThemePreviewItem(
                    theme = theme,
                    isSelected = theme.id == currentTheme.id,
                    isLocked = isLocked,
                    onSelected = {
                        if (isLocked) {
                            onShowPaywall()
                        } else {
                            onThemeSelected(theme)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewItem(
    theme: AppTheme,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .testTag("theme_item_${theme.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemePreviewSwatch(
                    player1Color = Color(theme.player1Color),
                    player2Color = Color(theme.player2Color),
                    modifier = Modifier.testTag("theme_preview_${theme.id}")
                )

                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Premium theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("selected_indicator_${theme.id}")
                        .semantics {
                            contentDescription = "${theme.name} theme selected"
                        }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewSwatch(
    player1Color: Color,
    player2Color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(player1Color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                .background(player2Color)
                .align(Alignment.BottomEnd)
        )
    }
}
