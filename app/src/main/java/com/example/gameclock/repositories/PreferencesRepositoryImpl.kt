package com.example.gameclock.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.TimeControl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override suspend fun saveTheme(theme: AppTheme) = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(theme)
        sharedPreferences.edit()
            .putString(KEY_SELECTED_THEME, jsonString)
            .apply()
    }
    
    override suspend fun getSelectedTheme(): AppTheme = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPreferences.getString(KEY_SELECTED_THEME, null)
            if (jsonString != null) {
                json.decodeFromString<AppTheme>(jsonString)
            } else {
                AppTheme.DEFAULT
            }
        } catch (e: Exception) {
            // If deserialization fails, return default theme and clear corrupted data
            sharedPreferences.edit().remove(KEY_SELECTED_THEME).apply()
            AppTheme.DEFAULT
        }
    }
    
    override suspend fun saveLastUsedTimeControl(timeControl: TimeControl) = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(timeControl)
        sharedPreferences.edit()
            .putString(KEY_LAST_USED_TIME_CONTROL, jsonString)
            .apply()
    }
    
    override suspend fun getLastUsedTimeControl(): TimeControl? = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPreferences.getString(KEY_LAST_USED_TIME_CONTROL, null)
            if (jsonString != null) {
                json.decodeFromString<TimeControl>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            // If deserialization fails, return null and clear corrupted data
            sharedPreferences.edit().remove(KEY_LAST_USED_TIME_CONTROL).apply()
            null
        }
    }
    
    companion object {
        private const val PREFS_NAME = "game_clock_preferences"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_LAST_USED_TIME_CONTROL = "last_used_time_control"
    }
}