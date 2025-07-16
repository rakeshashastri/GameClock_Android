package com.example.gameclock.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.gameclock.models.TimeControl
import org.junit.Rule
import org.junit.Test

/**
 * Integration test to verify that TimeControlBottomSheet and CustomTimeControlDialog
 * work together correctly in a typical user flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
class TimeControlIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timeControlFlow_worksEndToEnd() {
        // Given
        var selectedTimeControl: TimeControl? = null
        var customTimeControls by mutableStateOf(emptyList<TimeControl>())

        // When - Set up the integrated UI
        composeTestRule.setContent {
            var showBottomSheet by remember { mutableStateOf(true) }
            var showCustomDialog by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()

            // Bottom sheet for time control selection
            TimeControlBottomSheet(
                isVisible = showBottomSheet,
                sheetState = sheetState,
                recentTimeControls = emptyList(),
                customTimeControls = customTimeControls,
                onTimeControlSelected = { 
                    selectedTimeControl = it
                    showBottomSheet = false
                },
                onCustomTimeControlDelete = { timeControl ->
                    customTimeControls = customTimeControls.filter { it.id != timeControl.id }
                },
                onCreateCustomTimeControl = {
                    showCustomDialog = true
                },
                onDismiss = { showBottomSheet = false }
            )

            // Dialog for creating custom time controls
            CustomTimeControlDialog(
                isVisible = showCustomDialog,
                onDismiss = { showCustomDialog = false },
                onSave = { newTimeControl ->
                    customTimeControls = customTimeControls + newTimeControl
                    showCustomDialog = false
                },
                canSaveMoreCustomTimeControls = customTimeControls.size < 5
            )
        }

        // Then - Verify bottom sheet is displayed
        composeTestRule.onNodeWithText("Time Control").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Custom Time Control").assertIsDisplayed()

        // When - Click create custom time control
        composeTestRule.onNodeWithText("Create Custom Time Control").performClick()

        // Then - Custom dialog should appear
        composeTestRule.onNodeWithText("Create Custom Time Control").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minutes").assertIsDisplayed()

        // When - Create a custom time control
        composeTestRule.onNodeWithText("1-60").performTextInput("7")
        composeTestRule.onNodeWithText("0-60 (optional)").performTextInput("3")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then - Should return to bottom sheet with new custom time control
        composeTestRule.onNodeWithText("Time Control").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("7 min + 3 sec").assertIsDisplayed()

        // When - Select the custom time control
        composeTestRule.onNodeWithText("7 min + 3 sec").performClick()

        // Then - Time control should be selected
        assert(selectedTimeControl != null)
        assert(selectedTimeControl?.name == "7 min + 3 sec")
        assert(selectedTimeControl?.timeInSeconds == 420L)
        assert(selectedTimeControl?.incrementInSeconds == 3L)
    }

    @Test
    fun timeControlFlow_handlesMaximumCustomTimeControls() {
        // Given - 5 custom time controls (maximum)
        val maxCustomTimeControls = (1..5).map { i ->
            TimeControl(name = "Custom $i", timeInSeconds = i * 60L, incrementInSeconds = 0)
        }

        // When
        composeTestRule.setContent {
            var showBottomSheet by remember { mutableStateOf(true) }
            var showCustomDialog by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()

            TimeControlBottomSheet(
                isVisible = showBottomSheet,
                sheetState = sheetState,
                recentTimeControls = emptyList(),
                customTimeControls = maxCustomTimeControls,
                onTimeControlSelected = { },
                onCustomTimeControlDelete = { },
                onCreateCustomTimeControl = {
                    showCustomDialog = true
                },
                onDismiss = { }
            )

            CustomTimeControlDialog(
                isVisible = showCustomDialog,
                onDismiss = { showCustomDialog = false },
                onSave = { },
                canSaveMoreCustomTimeControls = maxCustomTimeControls.size < 5
            )
        }

        // Then - All custom time controls should be displayed
        composeTestRule.onNodeWithText("Custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom 5").assertIsDisplayed()

        // When - Try to create another custom time control
        composeTestRule.onNodeWithText("Create Custom Time Control").performClick()

        // Then - Should show limit warning and disable save
        composeTestRule.onNodeWithText("You have reached the maximum of 5 custom time controls").assertIsDisplayed()
    }

    @Test
    fun timeControlFlow_allowsPresetSelection() {
        // Given
        var selectedTimeControl: TimeControl? = null

        // When
        composeTestRule.setContent {
            var showBottomSheet by remember { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState()

            TimeControlBottomSheet(
                isVisible = showBottomSheet,
                sheetState = sheetState,
                recentTimeControls = emptyList(),
                customTimeControls = emptyList(),
                onTimeControlSelected = { 
                    selectedTimeControl = it
                },
                onCustomTimeControlDelete = { },
                onCreateCustomTimeControl = { },
                onDismiss = { }
            )
        }

        // Then - Preset categories should be displayed
        composeTestRule.onNodeWithText("Bullet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Blitz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rapid").assertIsDisplayed()

        // When - Select a blitz preset
        composeTestRule.onNodeWithText("5 min").performClick()

        // Then - Should select the correct time control
        assert(selectedTimeControl != null)
        assert(selectedTimeControl?.name == "5 min")
        assert(selectedTimeControl?.timeInSeconds == 300L)
        assert(selectedTimeControl?.incrementInSeconds == 0L)
    }
}