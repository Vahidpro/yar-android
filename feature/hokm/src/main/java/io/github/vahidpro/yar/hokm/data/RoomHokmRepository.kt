package io.github.vahidpro.yar.hokm.data

import androidx.room.withTransaction
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.HokmRepository
import io.github.vahidpro.yar.hokm.domain.HokmRound
import io.github.vahidpro.yar.hokm.domain.HokmRules
import io.github.vahidpro.yar.hokm.domain.RoundDraft
import io.github.vahidpro.yar.hokm.domain.TeamNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class RoomHokmRepository(private val database: HokmDatabase) : HokmRepository {
    private val dao = database.dao()

    override val games = dao.observeGames()
        .map { bundles -> bundles.map(GameBundle::toDomain) }
        .flowOn(Dispatchers.Default)

    override suspend fun ensureActiveGame(): Unit = database.withTransaction {
        if (dao.activeGame() == null) dao.insertGame(GameSettings().toEntity())
    }

    override suspend fun newGame(settings: GameSettings): Long = database.withTransaction {
        val clean = settings.validated()
        archiveActiveGame()
        dao.insertGame(clean.toEntity())
    }

    override suspend fun activateGame(gameId: Long): Unit = database.withTransaction {
        val target = checkNotNull(dao.game(gameId))
        if (target.activeKey == null) {
            archiveActiveGame()
            dao.updateGame(target.copy(activeKey = 1))
        }
    }

    override suspend fun updateSettings(gameId: Long, settings: GameSettings) =
        database.withTransaction {
            val game = requireActiveGame(gameId)
            val clean = settings.validated()
            if (clean.trickCount != game.trickCount) {
                check(!hasProgress(gameId))
            }
            dao.updateGame(
                game.copy(
                    trickCount = clean.trickCount,
                    firstTeam = clean.firstTeam,
                    secondTeam = clean.secondTeam,
                ),
            )
        }

    override suspend fun startRound(gameId: Long, announced: TeamNumbers) =
        database.withTransaction {
            val game = requireActiveGame(gameId)
            require(HokmRules.validAnnouncements(announced, game.trickCount))
            check(dao.draft(gameId) == null)
            dao.insertDraft(
                DraftEntity(
                    gameId = gameId,
                    firstAnnounced = announced.first,
                    secondAnnounced = announced.second,
                ),
            )
        }

    override suspend fun updateDraft(gameId: Long, draftId: Long, announced: TeamNumbers) =
        database.withTransaction {
            val game = requireActiveGame(gameId)
            require(HokmRules.validAnnouncements(announced, game.trickCount))
            val draft = requireDraft(gameId, draftId)
            dao.updateDraft(draft.copy(firstAnnounced = announced.first, secondAnnounced = announced.second))
        }

    override suspend fun finishRound(gameId: Long, draftId: Long, taken: TeamNumbers) =
        database.withTransaction {
            val game = requireActiveGame(gameId)
            require(HokmRules.validResult(taken, game.trickCount))
            val draft = requireDraft(gameId, draftId)
            dao.insertRound(
                RoundEntity(
                    gameId = gameId,
                    firstAnnounced = draft.firstAnnounced,
                    secondAnnounced = draft.secondAnnounced,
                    firstTaken = taken.first,
                    secondTaken = taken.second,
                ),
            )
            dao.deleteDraft(gameId)
        }

    override suspend fun updateRound(
        gameId: Long,
        roundId: Long,
        announced: TeamNumbers,
        taken: TeamNumbers,
    ) = database.withTransaction {
        val game = requireActiveGame(gameId)
        require(HokmRules.validAnnouncements(announced, game.trickCount))
        require(HokmRules.validResult(taken, game.trickCount))
        val round = checkNotNull(dao.round(gameId, roundId))
        dao.updateRound(
            round.copy(
                firstAnnounced = announced.first,
                secondAnnounced = announced.second,
                firstTaken = taken.first,
                secondTaken = taken.second,
            ),
        )
    }

    override suspend fun deleteRound(gameId: Long, roundId: Long) = database.withTransaction {
        requireActiveGame(gameId)
        check(dao.deleteRound(gameId, roundId) == 1)
    }

    override suspend fun resetGame(gameId: Long) = database.withTransaction {
        requireActiveGame(gameId)
        dao.deleteDraft(gameId)
        dao.deleteRounds(gameId)
    }

    private suspend fun requireActiveGame(gameId: Long): GameEntity =
        checkNotNull(dao.activeGame()).also { check(it.id == gameId) }

    private suspend fun requireDraft(gameId: Long, draftId: Long): DraftEntity =
        checkNotNull(dao.draft(gameId)).also { check(it.id == draftId) }

    private suspend fun hasProgress(gameId: Long): Boolean =
        dao.draft(gameId) != null || dao.roundCount(gameId) != 0

    private suspend fun archiveActiveGame() {
        val active = dao.activeGame() ?: return
        if (active.settings() == GameSettings() && !hasProgress(active.id)) {
            dao.deleteGame(active.id)
        } else {
            dao.updateGame(active.copy(activeKey = null))
        }
    }
}

private fun GameSettings.validated(): GameSettings {
    val clean = copy(firstTeam = firstTeam.trimmed(), secondTeam = secondTeam.trimmed())
    require(clean.trickCount in HokmRules.allowedTrickCounts)
    require(clean.firstTeam.hasValidNames() && clean.secondTeam.hasValidNames())
    return clean
}

private fun GameSettings.toEntity() = GameEntity(
    createdAt = System.currentTimeMillis(),
    trickCount = trickCount,
    firstTeam = firstTeam,
    secondTeam = secondTeam,
)

private fun GameBundle.toDomain() = HokmGame(
    id = game.id,
    createdAt = game.createdAt,
    isActive = game.activeKey != null,
    settings = game.settings(),
    draft = draft?.let { RoundDraft(it.id, TeamNumbers(it.firstAnnounced, it.secondAnnounced)) },
    rounds = rounds.sortedBy(RoundEntity::id).mapIndexed { index, round ->
        HokmRound(
            id = round.id,
            number = index + 1,
            announced = TeamNumbers(round.firstAnnounced, round.secondAnnounced),
            taken = TeamNumbers(round.firstTaken, round.secondTaken),
        )
    },
)
