
package com.example.gameclock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameclock.models.AppTheme
import com.example.gameclock.repositories.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(AppTheme.DEFAULT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    val availableThemes: List<AppTheme> = AppTheme.ALL_THEMES

    init {
        loadCurrentTheme()
    }

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveTheme(theme)
                _currentTheme.value = theme
            } catch (e: Exception) {
                // Handle error - could emit error state or log
                // For now, keep current theme unchanged
            }
        }
    }

    private fun loadCurrentTheme() {
        viewModelScope.launch {
            try {
                val savedTheme = preferencesRepository.getSelectedTheme()
                _currentTheme.value = savedTheme
            } catch (e: Exception) {
                // Handle error - fallback to default theme
                _currentTheme.value = AppTheme.DEFAULT
            }
        }
    }
}
