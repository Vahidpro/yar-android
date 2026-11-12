package io.github.vahidpro.yar.hokm.domain

internal data class ScoreTableLabels(
    val round: String,
    val type: String,
    val regular: String,
    val forced: String,
    val announced: String,
    val taken: String,
    val points: String,
    val total: String,
)

internal fun Appendable.writeScoreTable(
    game: HokmGame,
    firstName: String,
    secondName: String,
    labels: ScoreTableLabels,
) {
    val measures = listOf(labels.announced, labels.taken, labels.points)
    row(
        listOf(labels.round, labels.type) +
            measures.map { "$firstName · $it" } +
            measures.map { "$secondName · $it" },
    )
    game.rounds.forEach { round ->
        row(listOf(
            round.number.toString(),
            if (round.isForced) labels.forced else labels.regular,
            round.announced.first.toString(),
            round.taken.first.toString(),
            round.points.first.toString(),
            round.announced.second.toString(),
            round.taken.second.toString(),
            round.points.second.toString(),
        ))
    }
    row(listOf(labels.total, "", "", "", game.firstTotal.toString(), "", "", game.secondTotal.toString()))
}

private fun Appendable.row(fields: List<String>) {
    append(fields.joinToString(",") { field ->
        val startsWithFormula = field.trimStart().firstOrNull()?.let { it in "=+-@" } == true
        val text = if (startsWithFormula) "'$field" else field
        "\"" + text.replace("\"", "\"\"") + "\""
    })
    append("\r\n")
}
