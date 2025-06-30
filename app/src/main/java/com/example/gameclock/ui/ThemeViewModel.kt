package com.example.gameclock.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameclock.ui.theme.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val themeKey = stringPreferencesKey("theme")

    val theme: StateFlow<Theme> = context.dataStore.data
        .map { preferences ->
            Theme.valueOf(preferences[themeKey] ?: Theme.CLASSIC.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme.CLASSIC
        )

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[themeKey] = theme.name
            }
        }
    }
}