package io.github.vahidpro.yar.hokm.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HokmRulesTest {
    @Test
    fun overbookingRemainsValidAndTheMinimumAppliesToBothTeams() {
        val overbooked = TeamNumbers(8, 7)
        assertTrue(HokmRules.validAnnouncements(overbooked, 13, minimum = 7))
        assertTrue(HokmRules.hasGuaranteedMiss(overbooked, 13))
        assertFalse(HokmRules.hasGuaranteedMiss(TeamNumbers(7, 6), 13))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(10, 3), 13, minimum = 4))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(3, 10), 13, minimum = 4))
    }

    @Test
    fun forcedHandScheduleUsesTheGameIntervalAndTheConfiguredPair() {
        val standard = GameSettings().forcedRounds
        assertEquals(TeamNumbers(7, 7), standard.announcementsFor(5))
        assertEquals(null, standard.announcementsFor(4))
        assertEquals(10L, standard.nextFrom(6))
        val custom = ForcedRounds(interval = 7, announced = TeamNumbers(5, 6))
        assertEquals(null, custom.announcementsFor(5))
        assertEquals(TeamNumbers(5, 6), custom.announcementsFor(7))
        assertEquals(14L, custom.nextFrom(8))
        assertEquals(null, ForcedRounds(interval = 0).nextFrom(8))
        assertTrue(GameSettings(minimumAnnouncement = 5, forcedRounds = custom).hasValidRules())
        assertFalse(GameSettings(minimumAnnouncement = 6, forcedRounds = custom).hasValidRules())
    }

    @Test
    fun statisticsUseTheDifferenceFromEachTeamsOwnBid() {
        val rounds = listOf(
            HokmRound(1, 1, TeamNumbers(7, 6), TeamNumbers(9, 4)),
            HokmRound(2, 2, TeamNumbers(7, 6), TeamNumbers(7, 6)),
            HokmRound(3, 3, TeamNumbers(9, 5), TeamNumbers(6, 7)),
        )
        assertEquals(TeamStats(1, 1, 1, 2L, 3L), rounds.statsFor(TeamSide.First))
        assertEquals(TeamStats(1, 1, 1, 2L, 2L), rounds.statsFor(TeamSide.Second))
        assertEquals(2, rounds.statsFor(TeamSide.First).made)
    }

    @Test
    fun scoreTableKeepsBothBidsResultsAndTotals() {
        val game = HokmGame(
            1, 0, true, GameSettings(), null,
            listOf(HokmRound(1, 5, TeamNumbers(7, 7), TeamNumbers(8, 5), isForced = true)),
        )
        val csv = buildString {
            writeScoreTable(
                game, "Sun, Club", "=Moon",
                ScoreTableLabels("Hand", "Type", "Regular", "Forced", "Bid", "Won", "Points", "Total"),
            )
        }
        assertTrue(csv.contains("\"Sun, Club · Bid\""))
        assertTrue(csv.contains("\"'=Moon · Points\""))
        assertTrue(csv.contains("\"5\",\"Forced\",\"7\",\"8\",\"7\",\"7\",\"5\",\"0\""))
        assertTrue(csv.contains("\"Total\",\"\",\"\",\"\",\"7\",\"\",\"\",\"0\""))
    }

    @Test
    fun exceedingAnAnnouncementEarnsOnlyTheAnnouncedPoints() {
        assertEquals(TeamNumbers(7, 0), HokmRules.points(TeamNumbers(7, 6), TeamNumbers(8, 5)))
    }

    @Test
    fun bothTeamsCanEarnTheirAnnouncements() {
        assertEquals(TeamNumbers(7, 6), HokmRules.points(TeamNumbers(7, 6), TeamNumbers(7, 6)))
    }

    @Test
    fun overbookingAllowsOneOrBothTeamsToMiss() {
        assertTrue(HokmRules.validAnnouncements(TeamNumbers(10, 4), 13))
        assertEquals(TeamNumbers(0, 4), HokmRules.points(TeamNumbers(10, 4), TeamNumbers(9, 4)))
        assertEquals(TeamNumbers(0, 0), HokmRules.points(TeamNumbers(8, 7), TeamNumbers(7, 6)))
    }

    @Test
    fun announcementsMustBePossibleIndividuallyAndCoverTheRoundTogether() {
        assertTrue(HokmRules.validAnnouncements(TeamNumbers(0, 13), 13))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(7, 5), 13))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(14, 0), 13))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(-1, 13), 13))
        assertFalse(HokmRules.validAnnouncements(TeamNumbers(0, 0), 0))
    }

    @Test
    fun actualTricksMustAddUpExactlyForCustomRoundSizes() {
        assertTrue(HokmRules.validResult(TeamNumbers(2, 3), 5))
        assertFalse(HokmRules.validResult(TeamNumbers(2, 2), 5))
        assertFalse(HokmRules.validResult(TeamNumbers(2, 4), 5))
        assertFalse(HokmRules.validResult(TeamNumbers(-1, 6), 5))
    }

    @Test
    fun totalsIncludeMoreThanOneHundredRounds() {
        val rounds = (1..150).map { number ->
            HokmRound(number.toLong(), number, TeamNumbers(7, 6), TeamNumbers(8, 5))
        }
        val game = HokmGame(1, 0, true, GameSettings(), null, rounds)
        assertEquals(1050L, game.firstTotal)
        assertEquals(0L, game.secondTotal)
        assertEquals(150, game.rounds.size)
    }
}
