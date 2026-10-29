package tj.pythontaj

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.pythontaj.domain.ProgressCalculator
import tj.pythontaj.domain.StreakCalculator

class ProgressLogicTest {
    @Test fun newInstallationHasZeroProgress() {
        assertEquals(0, ProgressCalculator.percent(0, 6))
        assertEquals(0, ProgressCalculator.percent(0, 0))
        assertEquals(1, ProgressCalculator.level(0).first)
    }

    @Test fun percentIsComputedFromRealCounts() {
        assertEquals(50, ProgressCalculator.percent(3, 6))
        assertEquals(100, ProgressCalculator.percent(6, 6))
        assertEquals(100, ProgressCalculator.percent(9, 6)) // аз ҳад зиёд набояд бошад
    }

    @Test fun levelsAreMonotonic() {
        assertEquals(2, ProgressCalculator.level(10).first)
        assertEquals(3, ProgressCalculator.level(40).first)
        assertEquals(4, ProgressCalculator.level(75).first)
        assertEquals(5, ProgressCalculator.level(100).first)
    }

    @Test fun streakCountsConsecutiveDays() {
        assertEquals(0, StreakCalculator.currentStreak(emptySet(), 100))
        assertEquals(3, StreakCalculator.currentStreak(setOf(98L, 99L, 100L), 100))
        assertEquals(2, StreakCalculator.currentStreak(setOf(98L, 99L), 100)) // дирӯз ҳанӯз силсиларо нигоҳ медорад
        assertEquals(0, StreakCalculator.currentStreak(setOf(90L, 91L), 100))
        assertEquals(1, StreakCalculator.currentStreak(setOf(95L, 100L), 100))
    }
}
