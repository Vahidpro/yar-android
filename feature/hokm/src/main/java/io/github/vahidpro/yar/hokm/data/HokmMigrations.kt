package io.github.vahidpro.yar.hokm.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN minimumAnnouncement INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE games ADD COLUMN forcedInterval INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE games ADD COLUMN firstForcedAnnouncement INTEGER NOT NULL DEFAULT 7")
        db.execSQL("ALTER TABLE games ADD COLUMN secondForcedAnnouncement INTEGER NOT NULL DEFAULT 7")
        db.execSQL("ALTER TABLE games ADD COLUMN showScores INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE games ADD COLUMN finishedAt INTEGER")

        db.execSQL("ALTER TABLE rounds ADD COLUMN roundNumber INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE rounds ADD COLUMN minimumAnnouncement INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE rounds ADD COLUMN isForced INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            UPDATE rounds
            SET roundNumber = (
                SELECT COUNT(*) FROM rounds AS previous
                WHERE previous.gameId = rounds.gameId AND previous.id <= rounds.id
            )
            """.trimIndent(),
        )
        db.execSQL("DROP INDEX index_rounds_gameId")
        db.execSQL("CREATE UNIQUE INDEX index_rounds_gameId_roundNumber ON rounds(gameId, roundNumber)")

        db.execSQL("ALTER TABLE drafts ADD COLUMN roundNumber INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE drafts ADD COLUMN minimumAnnouncement INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE drafts ADD COLUMN isForced INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            UPDATE drafts
            SET roundNumber = COALESCE(
                (SELECT MAX(rounds.roundNumber) FROM rounds WHERE rounds.gameId = drafts.gameId), 0
            ) + 1
            """.trimIndent(),
        )
    }
}
