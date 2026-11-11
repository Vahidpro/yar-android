package io.github.vahidpro.yar.hokm.domain

data class Team(
    val name: String = "",
    val firstPlayer: String = "",
    val secondPlayer: String = "",
) {
    fun trimmed() = Team(name.trim(), firstPlayer.trim(), secondPlayer.trim())

    fun hasValidNames(): Boolean = listOf(name, firstPlayer, secondPlayer).all {
        it.length <= nameLimit
    }

    companion object {
        const val nameLimit = 36
    }
}

data class GameSettings(
    val firstTeam: Team = Team(),
    val secondTeam: Team = Team(),
    val trickCount: Int = HokmRules.defaultTrickCount,
)

data class RoundDraft(val id: Long, val announced: TeamNumbers)

data class HokmRound(
    val id: Long,
    val number: Int,
    val announced: TeamNumbers,
    val taken: TeamNumbers,
) {
    val points: TeamNumbers = HokmRules.points(announced, taken)
}

data class HokmGame(
    val id: Long,
    val createdAt: Long,
    val isActive: Boolean,
    val settings: GameSettings,
    val draft: RoundDraft?,
    val rounds: List<HokmRound>,
) {
    val firstTotal: Long = rounds.sumOf { it.points.first.toLong() }
    val secondTotal: Long = rounds.sumOf { it.points.second.toLong() }
    val hasProgress: Boolean get() = rounds.isNotEmpty() || draft != null
}
