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
import io.github.vahidpro.yar.design.YarLanguage
import io.github.vahidpro.yar.design.YarPalette
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

data class AppPreferences(
    val mode: AppearanceMode = AppearanceMode.System,
    val palette: YarPalette = YarPalette.Forest,
    val language: YarLanguage = YarLanguage.Persian,
)

private val Context.appearanceDataStore by preferencesDataStore(name = "appearance")
private val themeKey = stringPreferencesKey("theme")
private val paletteKey = stringPreferencesKey("palette")
private val languageKey = stringPreferencesKey("language")

class AppearanceStore(context: Context) {
    private val dataStore = context.applicationContext.appearanceDataStore

    val preferences = dataStore.data
        .catch {
            if (it !is IOException) throw it
            Log.e("AppearanceStore", "Could not read appearance settings", it)
            emit(emptyPreferences())
        }
        .map { preferences ->
            AppPreferences(
                mode = AppearanceMode.entries.firstOrNull { it.storageValue == preferences[themeKey] }
                    ?: AppearanceMode.System,
                palette = YarPalette.entries.firstOrNull { it.key == preferences[paletteKey] }
                    ?: YarPalette.Forest,
                language = YarLanguage.entries.firstOrNull { it.tag == preferences[languageKey] }
                    ?: YarLanguage.Persian,
            )
        }

    suspend fun updateMode(mode: AppearanceMode) {
        dataStore.edit { it[themeKey] = mode.storageValue }
    }

    suspend fun updatePalette(palette: YarPalette) {
        dataStore.edit { it[paletteKey] = palette.key }
    }

    suspend fun updateLanguage(language: YarLanguage) {
        dataStore.edit { it[languageKey] = language.tag }
    }
}

class AppearanceViewModel(private val store: AppearanceStore) : ViewModel() {
    val preferences = store.preferences.stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())
    private val _saveFailed = MutableStateFlow(false)
    val saveFailed = _saveFailed.asStateFlow()

    fun select(mode: AppearanceMode) = save { store.updateMode(mode) }

    fun selectPalette(palette: YarPalette) = save { store.updatePalette(palette) }

    fun selectLanguage(language: YarLanguage) = save { store.updateLanguage(language) }

    private fun save(update: suspend () -> Unit) {
        viewModelScope.launch {
            _saveFailed.value = false
            try {
                update()
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
