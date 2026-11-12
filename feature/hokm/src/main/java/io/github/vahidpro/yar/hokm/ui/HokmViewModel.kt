package io.github.vahidpro.yar.hokm.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.HokmRepository
import io.github.vahidpro.yar.hokm.domain.TeamNumbers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HokmGamesState {
    data object Loading : HokmGamesState
    data object LoadFailed : HokmGamesState
    data class Ready(val games: List<HokmGame>) : HokmGamesState {
        val active: HokmGame? get() = games.firstOrNull(HokmGame::isActive)
    }
}

class HokmViewModel(private val repository: HokmRepository) : ViewModel() {
    private val _games = MutableStateFlow<HokmGamesState>(HokmGamesState.Loading)
    private val _saving = MutableStateFlow(false)
    private val _saveFailed = MutableStateFlow(false)
    private var loadJob: Job? = null

    val games = _games.asStateFlow()
    val saving = _saving.asStateFlow()
    val saveFailed = _saveFailed.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _games.value = HokmGamesState.Loading
            try {
                repository.ensureActiveGame()
                repository.games.collect { _games.value = HokmGamesState.Ready(it) }
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                Log.e("HokmViewModel", "Could not load games", failure)
                _games.value = HokmGamesState.LoadFailed
            }
        }
    }

    fun newGame(settings: GameSettings, onSaved: (Long) -> Unit) =
        mutate(onSaved) { repository.newGame(settings) }

    fun activate(gameId: Long, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.activateGame(gameId) }

    fun updateSettings(gameId: Long, settings: GameSettings, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.updateSettings(gameId, settings) }

    fun setScoresVisible(gameId: Long, visible: Boolean) =
        mutate({}) { repository.setScoresVisible(gameId, visible) }

    fun finishGame(gameId: Long, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.finishGame(gameId) }

    fun startRound(gameId: Long, announced: TeamNumbers, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.startRound(gameId, announced) }

    fun updateDraft(gameId: Long, draftId: Long, announced: TeamNumbers, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.updateDraft(gameId, draftId, announced) }

    fun finishRound(gameId: Long, draftId: Long, taken: TeamNumbers, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.finishRound(gameId, draftId, taken) }

    fun updateRound(
        gameId: Long,
        roundId: Long,
        announced: TeamNumbers,
        taken: TeamNumbers,
        onSaved: () -> Unit,
    ) = mutate({ onSaved() }) { repository.updateRound(gameId, roundId, announced, taken) }

    fun deleteRound(gameId: Long, roundId: Long, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.deleteRound(gameId, roundId) }

    fun reset(gameId: Long, onSaved: () -> Unit) =
        mutate({ onSaved() }) { repository.resetGame(gameId) }

    fun clearSaveError() {
        _saveFailed.value = false
    }

    private fun <T> mutate(onSaved: (T) -> Unit, action: suspend () -> T) {
        if (_saving.value) return
        _saving.value = true
        _saveFailed.value = false
        viewModelScope.launch {
            val result = try {
                action()
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                Log.e("HokmViewModel", "Could not save a game change", failure)
                _saveFailed.value = true
                return@launch
            } finally {
                _saving.value = false
            }
            onSaved(result)
        }
    }

    companion object {
        fun factory(repository: HokmRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HokmViewModel(repository) }
        }
    }
}
