package io.github.vahidpro.yar.hokm.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.vahidpro.yar.hokm.domain.ForcedRounds
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.HokmRepository
import io.github.vahidpro.yar.hokm.domain.Team
import io.github.vahidpro.yar.hokm.domain.TeamNumbers
import io.github.vahidpro.yar.hokm.domain.TeamSide
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class HokmRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun draftAndScoresSurviveClosingAndReopeningTheDatabase() = runBlocking {
        val name = "hokm-test-" + UUID.randomUUID() + ".db"
        try {
            val id = openDatabase(name).useDatabase { database ->
                val repository = RoomHokmRepository(database)
                repository.newGame(GameSettings(firstTeam = Team(" آفتاب ", "سارا", "آرش"))).also {
                    repository.startRound(it, TeamNumbers(7, 6))
                }
            }
            openDatabase(name).useDatabase { database ->
                val repository = RoomHokmRepository(database)
                val game = repository.activeGame()
                assertEquals(id, game.id)
                assertEquals(Team("آفتاب", "سارا", "آرش"), game.settings.firstTeam)
                assertEquals(TeamNumbers(7, 6), game.draft?.announced)
                repository.finishRound(id, requireNotNull(game.draft).id, TeamNumbers(8, 5))
            }
            openDatabase(name).useDatabase { database ->
                val game = RoomHokmRepository(database).activeGame()
                assertEquals(7L, game.firstTotal)
                assertEquals(0L, game.secondTotal)
                assertNull(game.draft)
            }
        } finally {
            context.deleteDatabase(name)
        }
        Unit
    }

    @Test
    fun aRetryCannotRecordTheSameDraftTwice() = runBlocking {
        memoryDatabase().useDatabase { database ->
            val repository = RoomHokmRepository(database)
            repository.ensureActiveGame()
            val id = repository.activeGame().id
            repository.startRound(id, TeamNumbers(7, 6))
            val draftId = requireNotNull(repository.activeGame().draft).id

            val results = List(2) {
                async { runCatching { repository.finishRound(id, draftId, TeamNumbers(8, 5)) } }
            }.awaitAll()

            assertEquals(1, results.count { it.isSuccess })
            assertEquals(1, repository.activeGame().rounds.size)
        }
    }

    @Test
    fun editingAndResettingOneGameLeavesSavedGamesIntact() = runBlocking {
        memoryDatabase().useDatabase { database ->
            val repository = RoomHokmRepository(database)
            val original = GameSettings(firstTeam = Team("آفتاب"))
            val firstId = repository.newGame(original)
            repository.startRound(firstId, TeamNumbers(7, 6))
            repository.finishRound(firstId, requireNotNull(repository.activeGame().draft).id, TeamNumbers(8, 5))
            val round = repository.activeGame().rounds.single()
            repository.updateRound(firstId, round.id, TeamNumbers(7, 6), TeamNumbers(6, 7))
            repository.startRound(firstId, TeamNumbers(10, 4))

            val secondId = repository.newGame(GameSettings(trickCount = 5))
            repository.startRound(secondId, TeamNumbers(2, 3))
            repository.resetGame(secondId)
            assertFalse(repository.activeGame().hasProgress)

            val saved = repository.games.first().single { it.id == firstId }
            assertFalse(saved.isActive)
            assertEquals(0L, saved.firstTotal)
            assertEquals(6L, saved.secondTotal)
            assertEquals(TeamNumbers(10, 4), saved.draft?.announced)
            repository.activateGame(firstId)
            assertTrue(repository.activeGame().isActive)
            assertEquals(original, repository.activeGame().settings)

            repository.resetGame(firstId)
            assertFalse(repository.activeGame().hasProgress)
            assertEquals(original, repository.activeGame().settings)
            assertEquals(2, repository.games.first().size)
        }
    }

    @Test
    fun forcedHandsKeepTheirBidsWhenSettingsChangeAndFinishingPreservesTheResult() = runBlocking {
        memoryDatabase().useDatabase { database ->
            val repository = RoomHokmRepository(database)
            val options = GameSettings(minimumAnnouncement = 5, forcedRounds = ForcedRounds(2, TeamNumbers(9, 5)))
            val id = repository.newGame(options)
            assertTrue(runCatching { repository.startRound(id, TeamNumbers(9, 4)) }.isFailure)
            repository.startRound(id, TeamNumbers(7, 6))
            repository.finishRound(id, requireNotNull(repository.activeGame().draft).id, TeamNumbers(7, 6))

            assertTrue(runCatching { repository.startRound(id, TeamNumbers(7, 6)) }.isFailure)
            repository.startRound(id, TeamNumbers(9, 5))
            val draft = requireNotNull(repository.activeGame().draft)
            assertEquals(2, draft.number)
            assertTrue(draft.isForced)
            repository.updateSettings(id, options.copy(forcedRounds = ForcedRounds(7, TeamNumbers(7, 7))))
            assertTrue(runCatching { repository.updateDraft(id, draft.id, TeamNumbers(7, 7)) }.isFailure)
            repository.finishRound(id, draft.id, TeamNumbers(8, 5))
            assertEquals(TeamNumbers(9, 5), repository.activeGame().rounds.last().announced)

            repository.setScoresVisible(id, false)
            assertFalse(repository.activeGame().scoresVisible)
            repository.finishGame(id)
            val finished = repository.activeGame()
            assertTrue(finished.isFinished)
            assertTrue(finished.scoresVisible)
            assertEquals(7L, finished.firstTotal)
            assertEquals(11L, finished.secondTotal)
            assertEquals(TeamSide.Second, finished.leader)
            assertTrue(runCatching { repository.startRound(id, TeamNumbers(7, 6)) }.isFailure)
        }
    }

    private fun openDatabase(name: String) = Room.databaseBuilder(context, HokmDatabase::class.java, name)
        .addMigrations(MIGRATION_1_2).build()

    private fun memoryDatabase() = Room.inMemoryDatabaseBuilder(context, HokmDatabase::class.java).build()

    private suspend fun HokmRepository.activeGame(): HokmGame = games.first().single { it.isActive }

    private inline fun <T> HokmDatabase.useDatabase(block: (HokmDatabase) -> T): T {
        try {
            return block(this)
        } finally {
            close()
        }
    }
}
