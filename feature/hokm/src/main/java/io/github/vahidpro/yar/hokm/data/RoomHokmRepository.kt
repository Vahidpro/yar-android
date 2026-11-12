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
        check(target.finishedAt == null)
        if (target.activeKey == null) {
            archiveActiveGame()
            dao.updateGame(target.copy(activeKey = 1))
        }
    }

    override suspend fun updateSettings(gameId: Long, settings: GameSettings) =
        database.withTransaction {
            val game = requireEditableGame(gameId)
            val clean = settings.validated()
            if (clean.trickCount != game.trickCount) {
                check(!hasProgress(gameId))
            }
            dao.updateGame(
                game.copy(
                    trickCount = clean.trickCount,
                    firstTeam = clean.firstTeam,
                    secondTeam = clean.secondTeam,
                    minimumAnnouncement = clean.minimumAnnouncement,
                    forcedInterval = clean.forcedRounds.interval,
                    firstForcedAnnouncement = clean.forcedRounds.announced.first,
                    secondForcedAnnouncement = clean.forcedRounds.announced.second,
                    showScores = clean.showScores,
                ),
            )
        }

    override suspend fun setScoresVisible(gameId: Long, visible: Boolean) =
        database.withTransaction {
            val game = checkNotNull(dao.game(gameId))
            check(game.finishedAt == null)
            dao.updateGame(game.copy(showScores = visible))
        }

    override suspend fun finishGame(gameId: Long) =
        database.withTransaction {
            val game = requireEditableGame(gameId)
            dao.deleteDraft(gameId)
            dao.updateGame(game.copy(finishedAt = System.currentTimeMillis()))
        }

    override suspend fun startRound(gameId: Long, announced: TeamNumbers) =
        database.withTransaction {
            val game = requireEditableGame(gameId)
            check(dao.draft(gameId) == null)
            val number = dao.lastRoundNumber(gameId) + 1
            val forced = game.settings().forcedRounds.announcementsFor(number)
            require(
                if (forced != null) announced == forced
                else HokmRules.validAnnouncements(announced, game.trickCount, game.minimumAnnouncement),
            )
            dao.insertDraft(
                DraftEntity(
                    gameId = gameId,
                    firstAnnounced = announced.first,
                    secondAnnounced = announced.second,
                    roundNumber = number,
                    minimumAnnouncement = game.minimumAnnouncement,
                    isForced = forced != null,
                ),
            )
        }

    override suspend fun updateDraft(gameId: Long, draftId: Long, announced: TeamNumbers) =
        database.withTransaction {
            val game = requireEditableGame(gameId)
            val draft = requireDraft(gameId, draftId)
            check(!draft.isForced)
            require(HokmRules.validAnnouncements(announced, game.trickCount, draft.minimumAnnouncement))
            dao.updateDraft(draft.copy(firstAnnounced = announced.first, secondAnnounced = announced.second))
        }

    override suspend fun finishRound(gameId: Long, draftId: Long, taken: TeamNumbers) =
        database.withTransaction {
            val game = requireEditableGame(gameId)
            require(HokmRules.validResult(taken, game.trickCount))
            val draft = requireDraft(gameId, draftId)
            dao.insertRound(
                RoundEntity(
                    gameId = gameId,
                    firstAnnounced = draft.firstAnnounced,
                    secondAnnounced = draft.secondAnnounced,
                    firstTaken = taken.first,
                    secondTaken = taken.second,
                    roundNumber = draft.roundNumber,
                    minimumAnnouncement = draft.minimumAnnouncement,
                    isForced = draft.isForced,
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
        val game = requireEditableGame(gameId)
        val round = checkNotNull(dao.round(gameId, roundId))
        require(
            if (round.isForced) announced == TeamNumbers(round.firstAnnounced, round.secondAnnounced)
            else HokmRules.validAnnouncements(announced, game.trickCount, round.minimumAnnouncement),
        )
        require(HokmRules.validResult(taken, game.trickCount))
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
        requireEditableGame(gameId)
        check(dao.deleteRound(gameId, roundId) == 1)
    }

    override suspend fun resetGame(gameId: Long) = database.withTransaction {
        requireEditableGame(gameId)
        dao.deleteDraft(gameId)
        dao.deleteRounds(gameId)
    }

    private suspend fun requireEditableGame(gameId: Long): GameEntity =
        checkNotNull(dao.activeGame()).also { check(it.id == gameId && it.finishedAt == null) }

    private suspend fun requireDraft(gameId: Long, draftId: Long): DraftEntity =
        checkNotNull(dao.draft(gameId)).also { check(it.id == draftId) }

    private suspend fun hasProgress(gameId: Long): Boolean =
        dao.draft(gameId) != null || dao.roundCount(gameId) != 0

    private suspend fun archiveActiveGame() {
        val active = dao.activeGame() ?: return
        if (active.finishedAt == null && active.settings().isDefault && !hasProgress(active.id)) {
            dao.deleteGame(active.id)
        } else {
            dao.updateGame(active.copy(activeKey = null))
        }
    }
}

private fun GameSettings.validated(): GameSettings {
    val clean = copy(firstTeam = firstTeam.trimmed(), secondTeam = secondTeam.trimmed())
    require(clean.hasValidRules())
    require(clean.firstTeam.hasValidNames() && clean.secondTeam.hasValidNames())
    return clean
}

private fun GameSettings.toEntity() = GameEntity(
    createdAt = System.currentTimeMillis(),
    trickCount = trickCount,
    firstTeam = firstTeam,
    secondTeam = secondTeam,
    minimumAnnouncement = minimumAnnouncement,
    forcedInterval = forcedRounds.interval,
    firstForcedAnnouncement = forcedRounds.announced.first,
    secondForcedAnnouncement = forcedRounds.announced.second,
    showScores = showScores,
)

private fun GameBundle.toDomain() = HokmGame(
    id = game.id,
    createdAt = game.createdAt,
    isActive = game.activeKey != null,
    settings = game.settings(),
    draft = draft?.let {
        RoundDraft(
            it.id, TeamNumbers(it.firstAnnounced, it.secondAnnounced),
            it.roundNumber, it.minimumAnnouncement, it.isForced,
        )
    },
    rounds = rounds.sortedBy(RoundEntity::roundNumber).map { round ->
        HokmRound(
            id = round.id,
            number = round.roundNumber,
            announced = TeamNumbers(round.firstAnnounced, round.secondAnnounced),
            taken = TeamNumbers(round.firstTaken, round.secondTaken),
            minimumAnnouncement = round.minimumAnnouncement,
            isForced = round.isForced,
        )
    },
    finishedAt = game.finishedAt,
)
