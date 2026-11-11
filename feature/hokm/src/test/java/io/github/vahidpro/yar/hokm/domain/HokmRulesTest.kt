package io.github.vahidpro.yar.hokm.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HokmRulesTest {
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
