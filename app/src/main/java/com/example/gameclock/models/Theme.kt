
package com.example.gameclock.models

import kotlinx.serialization.Serializable

@Serializable
data class AppTheme(
    val id: String,
    val name: String,
    val player1Color: Long, // Color as Long for serialization
    val player2Color: Long,
    val isDark: Boolean = false
) {
    companion object {
        val DEFAULT = AppTheme(
            id = "default",
            name = "Default",
            player1Color = 0xFF1A1A1A,
            player2Color = 0xFFE5E5E5
        )
        
        val MODERN = AppTheme(
            id = "modern",
            name = "Modern",
            player1Color = 0xFF003366,
            player2Color = 0xFFFFCC00
        )
        
        val FOREST = AppTheme(
            id = "forest",
            name = "Forest",
            player1Color = 0xFF1A4D33,
            player2Color = 0xFFE5CC99
        )
        
        val OCEAN = AppTheme(
            id = "ocean",
            name = "Ocean",
            player1Color = 0xFF006666,
            player2Color = 0xFFCCE5FF
        )
        
        val ALL_THEMES = listOf(DEFAULT, MODERN, FOREST, OCEAN)
    }
}
