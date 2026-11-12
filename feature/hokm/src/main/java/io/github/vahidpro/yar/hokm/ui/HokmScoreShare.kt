package io.github.vahidpro.yar.hokm.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.vahidpro.yar.design.YarLanguage
import io.github.vahidpro.yar.design.digits
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.ScoreTableLabels
import io.github.vahidpro.yar.hokm.domain.TeamSide
import io.github.vahidpro.yar.hokm.domain.writeScoreTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class HokmScoreProvider : FileProvider()

internal suspend fun shareScoreTable(
    context: Context,
    game: HokmGame,
    firstName: String,
    secondName: String,
    language: YarLanguage,
) {
    val labels = ScoreTableLabels(
        round = context.getString(R.string.hokm_round_label),
        type = context.getString(R.string.hokm_hand_type),
        regular = context.getString(R.string.hokm_regular_hand),
        forced = context.getString(R.string.hokm_forced_hand),
        announced = context.getString(R.string.hokm_example_announced),
        taken = context.getString(R.string.hokm_example_taken),
        points = context.getString(R.string.hokm_example_points),
        total = context.getString(R.string.hokm_total_score),
    )
    val file = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "hokm-shares")
        directory.mkdirs()
        File.createTempFile("yar-scores-", ".csv", directory).also { export ->
            export.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.append('\uFEFF')
                writer.writeScoreTable(game, firstName, secondName, labels)
            }
        }
    }
    val winner = when (game.leader) {
        TeamSide.First -> firstName
        TeamSide.Second -> secondName
        null -> null
    }
    val status = if (winner == null) context.getString(R.string.hokm_tied)
    else context.getString(R.string.hokm_winner, winner)
    val summary = context.getString(
        R.string.hokm_share_summary,
        status,
        firstName,
        language.digits(game.firstTotal.toString()),
        secondName,
        language.digits(game.secondTotal.toString()),
    )
    val uri = FileProvider.getUriForFile(context, context.packageName + ".hokm.share", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, summary)
        clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.hokm_share_table)))
}
