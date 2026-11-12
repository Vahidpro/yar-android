package io.github.vahidpro.yar.hokm.domain

data class TeamNumbers(val first: Int, val second: Int) {
    val sum: Int get() = first + second
}

object HokmRules {
    const val defaultTrickCount = 13
    val allowedTrickCounts = 1..99

    fun validAnnouncementValues(announced: TeamNumbers, trickCount: Int, minimum: Int): Boolean =
        trickCount in allowedTrickCounts &&
            minimum in 0..trickCount &&
            announced.first in minimum..trickCount &&
            announced.second in minimum..trickCount

    fun validAnnouncements(announced: TeamNumbers, trickCount: Int, minimum: Int = 0): Boolean =
        validAnnouncementValues(announced, trickCount, minimum) && announced.sum >= trickCount

    fun hasGuaranteedMiss(announced: TeamNumbers, trickCount: Int): Boolean =
        announced.sum > trickCount

    fun validResult(taken: TeamNumbers, trickCount: Int): Boolean =
        trickCount in allowedTrickCounts &&
            taken.first in 0..trickCount &&
            taken.second in 0..trickCount &&
            taken.sum == trickCount

    fun points(announced: TeamNumbers, taken: TeamNumbers): TeamNumbers =
        TeamNumbers(
            first = if (taken.first >= announced.first) announced.first else 0,
            second = if (taken.second >= announced.second) announced.second else 0,
        )
}
