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
    
    override suspend fun saveLowTimeWarningEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putBoolean(KEY_LOW_TIME_WARNING, enabled)
            .apply()
    }

    override suspend fun getLowTimeWarningEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_LOW_TIME_WARNING, true)
    }

    override suspend fun saveLowTimeThreshold(thresholdMs: Long) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putLong(KEY_LOW_TIME_THRESHOLD, thresholdMs)
            .apply()
    }

    override suspend fun getLowTimeThreshold(): Long = withContext(Dispatchers.IO) {
        sharedPreferences.getLong(KEY_LOW_TIME_THRESHOLD, DEFAULT_LOW_TIME_THRESHOLD_MS)
    }

    override suspend fun saveTapSoundEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putBoolean(KEY_TAP_SOUND, enabled)
            .apply()
    }

    override suspend fun getTapSoundEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(KEY_TAP_SOUND, true)
    }

    companion object {
        private const val PREFS_NAME = "game_clock_preferences"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_LAST_USED_TIME_CONTROL = "last_used_time_control"
        private const val KEY_LOW_TIME_WARNING = "low_time_warning_enabled"
        private const val KEY_LOW_TIME_THRESHOLD = "low_time_threshold_ms"
        private const val KEY_TAP_SOUND = "tap_sound_enabled"
        const val DEFAULT_LOW_TIME_THRESHOLD_MS = 30_000L
    }
}