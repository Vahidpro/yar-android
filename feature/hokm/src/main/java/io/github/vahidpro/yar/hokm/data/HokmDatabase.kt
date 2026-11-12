package io.github.vahidpro.yar.hokm.data

import android.content.Context
import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.ForcedRounds
import io.github.vahidpro.yar.hokm.domain.HokmRepository
import io.github.vahidpro.yar.hokm.domain.Team
import io.github.vahidpro.yar.hokm.domain.TeamNumbers
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "games", indices = [Index(value = ["activeKey"], unique = true)])
internal data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    // SQLite permits multiple NULLs in a unique index, so only one game can own key 1.
    val activeKey: Int? = 1,
    val trickCount: Int,
    @Embedded(prefix = "first_") val firstTeam: Team,
    @Embedded(prefix = "second_") val secondTeam: Team,
    @ColumnInfo(defaultValue = "0") val minimumAnnouncement: Int,
    @ColumnInfo(defaultValue = "0") val forcedInterval: Int,
    @ColumnInfo(defaultValue = "7") val firstForcedAnnouncement: Int,
    @ColumnInfo(defaultValue = "7") val secondForcedAnnouncement: Int,
    @ColumnInfo(defaultValue = "1") val showScores: Boolean,
    val finishedAt: Long? = null,
) {
    fun settings() = GameSettings(
        firstTeam, secondTeam, trickCount, minimumAnnouncement,
        ForcedRounds(forcedInterval, TeamNumbers(firstForcedAnnouncement, secondForcedAnnouncement)),
        showScores,
    )
}

@Entity(
    tableName = "drafts",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["gameId"], unique = true)],
)
internal data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val firstAnnounced: Int,
    val secondAnnounced: Int,
    @ColumnInfo(defaultValue = "0") val roundNumber: Int,
    @ColumnInfo(defaultValue = "0") val minimumAnnouncement: Int,
    @ColumnInfo(defaultValue = "0") val isForced: Boolean,
)

@Entity(
    tableName = "rounds",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["gameId", "roundNumber"], unique = true)],
)
internal data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val firstAnnounced: Int,
    val secondAnnounced: Int,
    val firstTaken: Int,
    val secondTaken: Int,
    @ColumnInfo(defaultValue = "0") val roundNumber: Int,
    @ColumnInfo(defaultValue = "0") val minimumAnnouncement: Int,
    @ColumnInfo(defaultValue = "0") val isForced: Boolean,
)

internal data class GameBundle(
    @Embedded val game: GameEntity,
    @Relation(parentColumn = "id", entityColumn = "gameId") val draft: DraftEntity?,
    @Relation(parentColumn = "id", entityColumn = "gameId") val rounds: List<RoundEntity>,
)

@Dao
internal interface HokmDao {
    @Transaction
    @Query("SELECT * FROM games ORDER BY activeKey DESC, id DESC")
    fun observeGames(): Flow<List<GameBundle>>

    @Query("SELECT * FROM games WHERE activeKey = 1")
    suspend fun activeGame(): GameEntity?

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun game(id: Long): GameEntity?

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: Long)

    @Query("SELECT * FROM drafts WHERE gameId = :gameId")
    suspend fun draft(gameId: Long): DraftEntity?

    @Insert
    suspend fun insertDraft(draft: DraftEntity)

    @Update
    suspend fun updateDraft(draft: DraftEntity)

    @Query("DELETE FROM drafts WHERE gameId = :gameId")
    suspend fun deleteDraft(gameId: Long)

    @Query("SELECT COUNT(*) FROM rounds WHERE gameId = :gameId")
    suspend fun roundCount(gameId: Long): Int

    @Query("SELECT COALESCE(MAX(roundNumber), 0) FROM rounds WHERE gameId = :gameId")
    suspend fun lastRoundNumber(gameId: Long): Int

    @Query("SELECT * FROM rounds WHERE gameId = :gameId AND id = :roundId")
    suspend fun round(gameId: Long, roundId: Long): RoundEntity?

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE gameId = :gameId AND id = :roundId")
    suspend fun deleteRound(gameId: Long, roundId: Long): Int

    @Query("DELETE FROM rounds WHERE gameId = :gameId")
    suspend fun deleteRounds(gameId: Long)
}

@Database(entities = [GameEntity::class, DraftEntity::class, RoundEntity::class], version = 2)
internal abstract class HokmDatabase : RoomDatabase() {
    abstract fun dao(): HokmDao
}

fun openHokmRepository(context: Context): HokmRepository =
    RoomHokmRepository(
        Room.databaseBuilder(
            context.applicationContext,
            HokmDatabase::class.java,
            "yar-hokm.db",
        ).addMigrations(MIGRATION_1_2).build(),
    )
