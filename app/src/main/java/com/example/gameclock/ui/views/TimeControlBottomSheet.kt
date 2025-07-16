package com.example.gameclock.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gameclock.models.TimeControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeControlBottomSheet(
    isVisible: Boolean,
    sheetState: SheetState,
    recentTimeControls: List<TimeControl>,
    customTimeControls: List<TimeControl>,
    onTimeControlSelected: (TimeControl) -> Unit,
    onCustomTimeControlDelete: (TimeControl) -> Unit,
    onCreateCustomTimeControl: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            TimeControlBottomSheetContent(
                recentTimeControls = recentTimeControls,
                customTimeControls = customTimeControls,
                onTimeControlSelected = onTimeControlSelected,
                onCustomTimeControlDelete = onCustomTimeControlDelete,
                onCreateCustomTimeControl = onCreateCustomTimeControl,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun TimeControlBottomSheetContent(
    recentTimeControls: List<TimeControl>,
    customTimeControls: List<TimeControl>,
    onTimeControlSelected: (TimeControl) -> Unit,
    onCustomTimeControlDelete: (TimeControl) -> Unit,
    onCreateCustomTimeControl: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Time Control",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Recent Time Controls Section
        if (recentTimeControls.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent")
            }
            
            items(recentTimeControls) { timeControl ->
                TimeControlItem(
                    timeControl = timeControl,
                    onClick = { 
                        onTimeControlSelected(timeControl)
                        onDismiss()
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Custom Time Controls Section
        if (customTimeControls.isNotEmpty()) {
            item {
                SectionHeader(title = "Custom")
            }
            
            items(customTimeControls) { timeControl ->
                TimeControlItem(
                    timeControl = timeControl,
                    onClick = { 
                        onTimeControlSelected(timeControl)
                        onDismiss()
                    },
                    onDelete = { onCustomTimeControlDelete(timeControl) },
                    showDeleteButton = true
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bullet Presets Section
        item {
            SectionHeader(title = "Bullet")
        }
        
        items(TimeControl.BULLET_PRESETS) { timeControl ->
            TimeControlItem(
                timeControl = timeControl,
                onClick = { 
                    onTimeControlSelected(timeControl)
                    onDismiss()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Blitz Presets Section
        item {
            SectionHeader(title = "Blitz")
        }
        
        items(TimeControl.BLITZ_PRESETS) { timeControl ->
            TimeControlItem(
                timeControl = timeControl,
                onClick = { 
                    onTimeControlSelected(timeControl)
                    onDismiss()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Rapid Presets Section
        item {
            SectionHeader(title = "Rapid")
        }
        
        items(TimeControl.RAPID_PRESETS) { timeControl ->
            TimeControlItem(
                timeControl = timeControl,
                onClick = { 
                    onTimeControlSelected(timeControl)
                    onDismiss()
                }
            )
        }

        // Create Custom Time Control Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        onCreateCustomTimeControl()
                        onDismiss()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom time control",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Custom Time Control",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun TimeControlItem(
    timeControl: TimeControl,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    showDeleteButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = timeControl.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                val timeText = formatTimeDisplay(timeControl.timeInSeconds)
                val incrementText = if (timeControl.incrementInSeconds > 0) {
                    " + ${timeControl.incrementInSeconds}s"
                } else {
                    ""
                }
                
                Text(
                    text = "$timeText$incrementText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (showDeleteButton && onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${timeControl.name}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatTimeDisplay(timeInSeconds: Long): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    return if (seconds == 0L) {
        "${minutes}m"
    } else {
        "${minutes}m ${seconds}s"
    }
}