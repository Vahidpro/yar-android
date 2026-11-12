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
    val minimumAnnouncement: Int = 0,
    val forcedRounds: ForcedRounds = ForcedRounds(
        announced = TeamNumbers(minOf(7, trickCount), minOf(7, trickCount)),
    ),
    val showScores: Boolean = true,
) {
    fun hasValidRules(): Boolean =
        trickCount in HokmRules.allowedTrickCounts &&
            minimumAnnouncement in 0..trickCount &&
            forcedRounds.interval >= 0 &&
            (forcedRounds.interval == 0 ||
                HokmRules.validAnnouncementValues(forcedRounds.announced, trickCount, minimumAnnouncement))

    val isDefault: Boolean get() = this == GameSettings() || this == GameSettings(
        forcedRounds = ForcedRounds(interval = 0),
    )
}

data class ForcedRounds(
    val interval: Int = 5,
    val announced: TeamNumbers = TeamNumbers(7, 7),
) {
    fun announcementsFor(number: Int): TeamNumbers? =
        announced.takeIf { interval > 0 && number % interval == 0 }

    fun nextFrom(number: Int): Long? {
        if (interval <= 0) return null
        return number.toLong() + (interval - number % interval) % interval
    }
}

data class RoundDraft(
    val id: Long,
    val announced: TeamNumbers,
    val number: Int,
    val minimumAnnouncement: Int,
    val isForced: Boolean,
)

data class HokmRound(
    val id: Long,
    val number: Int,
    val announced: TeamNumbers,
    val taken: TeamNumbers,
    val minimumAnnouncement: Int = 0,
    val isForced: Boolean = false,
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
    val finishedAt: Long? = null,
) {
    val firstTotal: Long = rounds.sumOf { it.points.first.toLong() }
    val secondTotal: Long = rounds.sumOf { it.points.second.toLong() }
    val hasProgress: Boolean get() = rounds.isNotEmpty() || draft != null
    val isFinished: Boolean get() = finishedAt != null
    val isEditable: Boolean get() = isActive && !isFinished
    val scoresVisible: Boolean get() = settings.showScores || isFinished
    val nextRoundNumber: Int = draft?.number ?: ((rounds.maxOfOrNull(HokmRound::number) ?: 0) + 1)
    val leader: TeamSide? get() = when {
        firstTotal > secondTotal -> TeamSide.First
        secondTotal > firstTotal -> TeamSide.Second
        else -> null
    }
}

enum class TeamSide { First, Second }

data class TeamStats(
    val exact: Int,
    val underbid: Int,
    val missed: Int,
    val excessTricks: Long,
    val shortfall: Long,
) {
    val made: Int get() = exact + underbid
}

fun List<HokmRound>.statsFor(side: TeamSide): TeamStats {
    val differences = map { round ->
        when (side) {
            TeamSide.First -> round.taken.first - round.announced.first
            TeamSide.Second -> round.taken.second - round.announced.second
        }
    }
    return TeamStats(
        exact = differences.count { it == 0 },
        underbid = differences.count { it > 0 },
        missed = differences.count { it < 0 },
        excessTricks = differences.sumOf { it.coerceAtLeast(0).toLong() },
        shortfall = differences.sumOf { (-it).coerceAtLeast(0).toLong() },
    )
}
