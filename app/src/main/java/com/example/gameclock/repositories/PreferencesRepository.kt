package com.example.gameclock.repositories

import com.example.gameclock.models.AppTheme
import com.example.gameclock.models.TimeControl

interface PreferencesRepository {
    suspend fun saveTheme(theme: AppTheme)
    suspend fun getSelectedTheme(): AppTheme
    suspend fun saveLastUsedTimeControl(timeControl: TimeControl)
    suspend fun getLastUsedTimeControl(): TimeControl?
}