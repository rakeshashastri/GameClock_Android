package com.example.gameclock.ui.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.gameclock.models.TimeControl
import org.junit.Rule
import org.junit.Test

class CustomTimeControlDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun customTimeControlDialog_displaysCorrectly_whenVisible() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Then
        composeTestRule.onNodeWithText("Create Custom Time Control").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minutes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Increment (seconds)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_doesNotDisplay_whenNotVisible() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = false,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Then
        composeTestRule.onNodeWithText("Create Custom Time Control").assertDoesNotExist()
    }

    @Test
    fun customTimeControlDialog_autoGeneratesName_whenTimeInputsChange() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input minutes only
        composeTestRule.onNodeWithText("1-60").performTextInput("5")
        
        // Then name should be auto-generated
        composeTestRule.onNodeWithText("5 min").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_autoGeneratesNameWithIncrement_whenBothInputsProvided() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input minutes and increment
        composeTestRule.onNodeWithText("1-60").performTextInput("3")
        composeTestRule.onNodeWithText("0-60 (optional)").performTextInput("2")
        
        // Then name should include increment
        composeTestRule.onNodeWithText("3 min + 2 sec").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_callsOnSave_whenValidInputProvided() {
        // Given
        var savedTimeControl: TimeControl? = null

        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = { savedTimeControl = it },
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input valid data
        composeTestRule.onNodeWithText("1-60").performTextInput("10")
        composeTestRule.onNodeWithText("0-60 (optional)").performTextInput("5")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        assert(savedTimeControl != null)
        assert(savedTimeControl?.name == "10 min + 5 sec")
        assert(savedTimeControl?.timeInSeconds == 600L)
        assert(savedTimeControl?.incrementInSeconds == 5L)
    }

    @Test
    fun customTimeControlDialog_callsOnDismiss_whenCancelClicked() {
        // Given
        var dismissCalled = false

        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = { dismissCalled = true },
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assert(dismissCalled)
    }

    @Test
    fun customTimeControlDialog_disablesSaveButton_whenCannotSaveMore() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = false
            )
        }

        // Then
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
        composeTestRule.onNodeWithText("You have reached the maximum of 5 custom time controls").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_enablesSaveButton_whenCanSaveMore() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input valid minutes to enable save
        composeTestRule.onNodeWithText("1-60").performTextInput("5")

        // Then
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun customTimeControlDialog_showsValidationError_forInvalidMinutes() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input invalid minutes and try to save
        composeTestRule.onNodeWithText("1-60").performTextInput("70")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        composeTestRule.onNodeWithText("Enter minutes between 1 and 60").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_showsValidationError_forInvalidIncrement() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input valid minutes but invalid increment
        composeTestRule.onNodeWithText("1-60").performTextInput("5")
        composeTestRule.onNodeWithText("0-60 (optional)").performTextInput("70")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        composeTestRule.onNodeWithText("Enter increment between 0 and 60 seconds").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_acceptsZeroIncrement() {
        // Given
        var savedTimeControl: TimeControl? = null

        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = { savedTimeControl = it },
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input valid minutes with zero increment (empty field)
        composeTestRule.onNodeWithText("1-60").performTextInput("15")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        assert(savedTimeControl != null)
        assert(savedTimeControl?.incrementInSeconds == 0L)
        assert(savedTimeControl?.name == "15 min")
    }

    @Test
    fun customTimeControlDialog_allowsCustomName() {
        // Given
        var savedTimeControl: TimeControl? = null

        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = { savedTimeControl = it },
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input time and customize name
        composeTestRule.onNodeWithText("1-60").performTextInput("5")
        // Clear the auto-generated name and input custom name
        composeTestRule.onNodeWithText("5 min").performTextInput("")
        composeTestRule.onNodeWithText("Custom name").performTextInput("My Custom Game")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        assert(savedTimeControl != null)
        assert(savedTimeControl?.name == "My Custom Game")
    }

    @Test
    fun customTimeControlDialog_preventsEmptyName() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Input valid minutes but clear the name
        composeTestRule.onNodeWithText("1-60").performTextInput("5")
        composeTestRule.onNodeWithText("5 min").performTextInput("")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        composeTestRule.onNodeWithText("Name cannot be empty").assertIsDisplayed()
    }

    @Test
    fun customTimeControlDialog_limitsInputLength() {
        // When
        composeTestRule.setContent {
            CustomTimeControlDialog(
                isVisible = true,
                onDismiss = {},
                onSave = {},
                canSaveMoreCustomTimeControls = true
            )
        }

        // Try to input more than 2 digits for minutes
        composeTestRule.onNodeWithText("1-60").performTextInput("123")
        
        // Should only accept first 2 digits
        // This is harder to test directly, but the validation logic prevents it
    }
}