package tj.pythontaj.domain

/** Ҳисоби пешрафт аз маълумоти воқеии захирашуда (на рақамҳои сохта). */
object ProgressCalculator {
    fun percent(completed: Int, total: Int): Int =
        if (total <= 0) 0 else (completed.coerceIn(0, total) * 100) / total

    /** Сатҳ танҳо аз дарсҳои анҷомшуда меояд; дарс бо иҷрои ҳамаи машқҳояш анҷом мешавад. */
    fun level(percent: Int): Pair<Int, String> = when {
        percent >= 100 -> 5 to "Анҷомёфта"
        percent >= 75 -> 4 to "Пешрафта"
        percent >= 40 -> 3 to "Миёна"
        percent >= 10 -> 2 to "Асосҳо"
        else -> 1 to "Оғоз"
    }
}

object StreakCalculator {
    /** [days] ва [today] — рақами рӯз (epochDay). Силсила то имрӯз ё дирӯз ҳисоб мешавад. */
    fun currentStreak(days: Set<Long>, today: Long): Int {
        var cursor = when {
            today in days -> today
            (today - 1) in days -> today - 1
            else -> return 0
        }
        var count = 0
        while (cursor in days) {
            count++
            cursor--
        }
        return count
    }
}
