package com.example.gameclock

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gameclock.ui.ThemeViewModel
import com.example.gameclock.repositories.PreferencesRepositoryImpl

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(PreferencesRepositoryImpl(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}