package io.github.vahidpro.yar.hokm.domain

import kotlinx.coroutines.flow.Flow

interface HokmRepository {
    val games: Flow<List<HokmGame>>

    suspend fun ensureActiveGame()
    suspend fun newGame(settings: GameSettings): Long
    suspend fun activateGame(gameId: Long)
    suspend fun updateSettings(gameId: Long, settings: GameSettings)
    suspend fun setScoresVisible(gameId: Long, visible: Boolean)
    suspend fun finishGame(gameId: Long)
    suspend fun startRound(gameId: Long, announced: TeamNumbers)
    suspend fun updateDraft(gameId: Long, draftId: Long, announced: TeamNumbers)
    suspend fun finishRound(gameId: Long, draftId: Long, taken: TeamNumbers)
    suspend fun updateRound(gameId: Long, roundId: Long, announced: TeamNumbers, taken: TeamNumbers)
    suspend fun deleteRound(gameId: Long, roundId: Long)
    suspend fun resetGame(gameId: Long)
}
