
package com.example.gameclock.services

import android.content.Context
import com.example.gameclock.models.AppTheme

class ThemeManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)

    fun getSelectedTheme(): AppTheme {
        val themeId = sharedPreferences.getString("selectedTheme", "default") ?: "default"
        return AppTheme.ALL_THEMES.first { it.id == themeId }
    }

    fun saveTheme(theme: AppTheme) {
        sharedPreferences.edit().putString("selectedTheme", theme.id).apply()
    }
}
