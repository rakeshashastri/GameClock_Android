package com.example.gameclock.repositories

import com.example.gameclock.models.TimeControl

interface GameRepository {
    suspend fun saveRecentTimeControl(timeControl: TimeControl)
    suspend fun getRecentTimeControls(): List<TimeControl>
    suspend fun saveCustomTimeControl(timeControl: TimeControl)
    suspend fun getCustomTimeControls(): List<TimeControl>
    suspend fun deleteCustomTimeControl(timeControl: TimeControl)
}