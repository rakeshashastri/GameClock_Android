package com.example.gameclock

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gameclock.repositories.GameRepository
import com.example.gameclock.repositories.PreferencesRepository
import com.example.gameclock.ui.GameViewModel

class GameViewModelFactory(
    private val gameRepository: GameRepository,
    private val preferencesRepository: PreferencesRepository,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(gameRepository, preferencesRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
