package com.example.gameclock.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.gameclock.models.TimeControl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepositoryImpl(context: Context) : GameRepository {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override suspend fun saveRecentTimeControl(timeControl: TimeControl) = withContext(Dispatchers.IO) {
        val currentRecent = getRecentTimeControls().toMutableList()
        
        // Remove if already exists to avoid duplicates
        currentRecent.removeAll { it.id == timeControl.id }
        
        // Add to beginning of list
        currentRecent.add(0, timeControl)
        
        // Keep only the 3 most recent
        val limitedRecent = currentRecent.take(MAX_RECENT_TIME_CONTROLS)
        
        val jsonString = json.encodeToString(limitedRecent)
        sharedPreferences.edit()
            .putString(KEY_RECENT_TIME_CONTROLS, jsonString)
            .apply()
    }
    
    override suspend fun getRecentTimeControls(): List<TimeControl> = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPreferences.getString(KEY_RECENT_TIME_CONTROLS, null)
            if (jsonString != null) {
                json.decodeFromString<List<TimeControl>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // If deserialization fails, return empty list and clear corrupted data
            sharedPreferences.edit().remove(KEY_RECENT_TIME_CONTROLS).apply()
            emptyList()
        }
    }
    
    override suspend fun saveCustomTimeControl(timeControl: TimeControl) = withContext(Dispatchers.IO) {
        val currentCustom = getCustomTimeControls().toMutableList()
        
        // Check if we've reached the limit
        if (currentCustom.size >= MAX_CUSTOM_TIME_CONTROLS && 
            !currentCustom.any { it.id == timeControl.id }) {
            throw IllegalStateException("Cannot add more than $MAX_CUSTOM_TIME_CONTROLS custom time controls")
        }
        
        // Remove if already exists (for updates)
        currentCustom.removeAll { it.id == timeControl.id }
        
        // Add the new/updated time control
        currentCustom.add(timeControl)
        
        val jsonString = json.encodeToString(currentCustom)
        sharedPreferences.edit()
            .putString(KEY_CUSTOM_TIME_CONTROLS, jsonString)
            .apply()
    }
    
    override suspend fun getCustomTimeControls(): List<TimeControl> = withContext(Dispatchers.IO) {
        try {
            val jsonString = sharedPreferences.getString(KEY_CUSTOM_TIME_CONTROLS, null)
            if (jsonString != null) {
                json.decodeFromString<List<TimeControl>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // If deserialization fails, return empty list and clear corrupted data
            sharedPreferences.edit().remove(KEY_CUSTOM_TIME_CONTROLS).apply()
            emptyList()
        }
    }
    
    override suspend fun deleteCustomTimeControl(timeControl: TimeControl) = withContext(Dispatchers.IO) {
        val currentCustom = getCustomTimeControls().toMutableList()
        currentCustom.removeAll { it.id == timeControl.id }
        
        val jsonString = json.encodeToString(currentCustom)
        sharedPreferences.edit()
            .putString(KEY_CUSTOM_TIME_CONTROLS, jsonString)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "game_clock_prefs"
        private const val KEY_RECENT_TIME_CONTROLS = "recent_time_controls"
        private const val KEY_CUSTOM_TIME_CONTROLS = "custom_time_controls"
        private const val MAX_RECENT_TIME_CONTROLS = 3
        private const val MAX_CUSTOM_TIME_CONTROLS = 5
    }
}