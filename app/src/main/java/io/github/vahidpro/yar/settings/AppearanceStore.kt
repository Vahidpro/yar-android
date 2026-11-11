package io.github.vahidpro.yar.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

enum class AppearanceMode(val storageValue: String) {
    System("system"), Light("light"), Dark("dark"),
}

private val Context.appearanceDataStore by preferencesDataStore(name = "appearance")
private val themeKey = stringPreferencesKey("theme")

class AppearanceStore(context: Context) {
    private val dataStore = context.applicationContext.appearanceDataStore

    val mode = dataStore.data
        .catch {
            if (it !is IOException) throw it
            Log.e("AppearanceStore", "Could not read appearance settings", it)
            emit(emptyPreferences())
        }
        .map { preferences ->
            AppearanceMode.entries.firstOrNull { it.storageValue == preferences[themeKey] }
                ?: AppearanceMode.System
        }

    suspend fun updateMode(mode: AppearanceMode) {
        dataStore.edit { it[themeKey] = mode.storageValue }
    }
}

class AppearanceViewModel(private val store: AppearanceStore) : ViewModel() {
    val mode = store.mode.stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceMode.System)
    private val _saveFailed = MutableStateFlow(false)
    val saveFailed = _saveFailed.asStateFlow()

    fun select(mode: AppearanceMode) {
        viewModelScope.launch {
            _saveFailed.value = false
            try {
                store.updateMode(mode)
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                Log.e("AppearanceViewModel", "Could not save appearance", failure)
                _saveFailed.value = true
            }
        }
    }

    fun clearSaveError() {
        _saveFailed.value = false
    }

    companion object {
        fun factory(store: AppearanceStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppearanceViewModel(store) }
        }
    }
}
