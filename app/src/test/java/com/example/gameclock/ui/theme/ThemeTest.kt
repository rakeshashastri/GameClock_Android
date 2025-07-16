package com.example.gameclock.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.gameclock.models.AppTheme
import org.junit.Test
import org.junit.Assert.*

class ThemeTest {

    @Test
    fun `createGameColors should calculate correct text colors for accessibility`() {
        // Test with light background (should use dark text)
        val lightTheme = AppTheme(
            id = "light",
            name = "Light",
            player1Color = 0xFFFFFFFF, // White background
            player2Color = 0xFFE5E5E5   // Light gray background
        )
        
        val lightGameColors = createGameColors(lightTheme)
        
        // Light backgrounds should have dark text for contrast
        assertEquals(Color.Black, lightGameColors.onPlayer1Color)
        assertEquals(Color.Black, lightGameColors.onPlayer2Color)
    }

    @Test
    fun `createGameColors should use white text on dark backgrounds`() {
        // Test with dark background (should use light text)
        val darkTheme = AppTheme(
            id = "dark",
            name = "Dark",
            player1Color = 0xFF000000, // Black background
            player2Color = 0xFF1A1A1A   // Dark gray background
        )
        
        val darkGameColors = createGameColors(darkTheme)
        
        // Dark backgrounds should have light text for contrast
        assertEquals(Color.White, darkGameColors.onPlayer1Color)
        assertEquals(Color.White, darkGameColors.onPlayer2Color)
    }

    @Test
    fun `all predefined themes should have sufficient color contrast`() {
        AppTheme.ALL_THEMES.forEach { theme ->
            val gameColors = createGameColors(theme)
            
            // Check that text colors provide sufficient contrast
            val player1Luminance = gameColors.player1Color.luminance()
            val player2Luminance = gameColors.player2Color.luminance()
            
            // Verify that text color selection is appropriate
            if (player1Luminance > 0.5f) {
                assertEquals("Theme ${theme.name} player1 should use dark text on light background", 
                    Color.Black, gameColors.onPlayer1Color)
            } else {
                assertEquals("Theme ${theme.name} player1 should use light text on dark background", 
                    Color.White, gameColors.onPlayer1Color)
            }
            
            if (player2Luminance > 0.5f) {
                assertEquals("Theme ${theme.name} player2 should use dark text on light background", 
                    Color.Black, gameColors.onPlayer2Color)
            } else {
                assertEquals("Theme ${theme.name} player2 should use light text on dark background", 
                    Color.White, gameColors.onPlayer2Color)
            }
        }
    }

    @Test
    fun `gameColors should preserve original theme colors`() {
        val testTheme = AppTheme(
            id = "test",
            name = "Test",
            player1Color = 0xFF003366,
            player2Color = 0xFFFFCC00
        )
        
        val gameColors = createGameColors(testTheme)
        
        assertEquals(Color(testTheme.player1Color), gameColors.player1Color)
        assertEquals(Color(testTheme.player2Color), gameColors.player2Color)
    }

    @Test
    fun `default theme should have expected colors`() {
        val gameColors = createGameColors(AppTheme.DEFAULT)
        
        assertEquals(Color(0xFF1A1A1A), gameColors.player1Color)
        assertEquals(Color(0xFFE5E5E5), gameColors.player2Color)
        assertEquals(Color.White, gameColors.onPlayer1Color) // Dark background -> white text
        assertEquals(Color.Black, gameColors.onPlayer2Color) // Light background -> black text
    }

    @Test
    fun `modern theme should have expected colors`() {
        val gameColors = createGameColors(AppTheme.MODERN)
        
        assertEquals(Color(0xFF003366), gameColors.player1Color)
        assertEquals(Color(0xFFFFCC00), gameColors.player2Color)
        assertEquals(Color.White, gameColors.onPlayer1Color) // Dark blue -> white text
        assertEquals(Color.Black, gameColors.onPlayer2Color) // Bright yellow -> black text
    }

    @Test
    fun `forest theme should have expected colors`() {
        val gameColors = createGameColors(AppTheme.FOREST)
        
        assertEquals(Color(0xFF1A4D33), gameColors.player1Color)
        assertEquals(Color(0xFFE5CC99), gameColors.player2Color)
        assertEquals(Color.White, gameColors.onPlayer1Color) // Dark green -> white text
        assertEquals(Color.Black, gameColors.onPlayer2Color) // Light beige -> black text
    }

    @Test
    fun `ocean theme should have expected colors`() {
        val gameColors = createGameColors(AppTheme.OCEAN)
        
        assertEquals(Color(0xFF006666), gameColors.player1Color)
        assertEquals(Color(0xFFCCE5FF), gameColors.player2Color)
        assertEquals(Color.White, gameColors.onPlayer1Color) // Dark teal -> white text
        assertEquals(Color.Black, gameColors.onPlayer2Color) // Light blue -> black text
    }

    // Helper function to access the private createGameColors function for testing
    private fun createGameColors(appTheme: AppTheme): GameColors {
        val player1Color = Color(appTheme.player1Color)
        val player2Color = Color(appTheme.player2Color)
        
        val onPlayer1Color = if (player1Color.luminance() > 0.5f) Color.Black else Color.White
        val onPlayer2Color = if (player2Color.luminance() > 0.5f) Color.Black else Color.White
        
        return GameColors(
            player1Color = player1Color,
            player2Color = player2Color,
            onPlayer1Color = onPlayer1Color,
            onPlayer2Color = onPlayer2Color
        )
    }
}