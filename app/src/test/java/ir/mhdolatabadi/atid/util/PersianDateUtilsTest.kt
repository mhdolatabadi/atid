package ir.mhdolatabadi.atid.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Same reference dates as web/src/lib/__tests__/persianDate.test.ts (from `jdatetime`). */
class PersianDateUtilsTest {

    private data class Reference(val gy: Int, val gm: Int, val gd: Int, val jy: Int, val jm: Int, val jd: Int, val weekday: Int)

    // weekday is java.util.Calendar's constant (SUNDAY = 1 .. SATURDAY = 7).
    private val references = listOf(
        Reference(1925, 3, 21, 1304, 1, 1, Calendar.SATURDAY),
        Reference(1979, 2, 11, 1357, 11, 22, Calendar.SUNDAY),
        Reference(2000, 1, 1, 1378, 10, 11, Calendar.SATURDAY),
        Reference(2024, 3, 19, 1402, 12, 29, Calendar.TUESDAY),
        Reference(2024, 3, 20, 1403, 1, 1, Calendar.WEDNESDAY),
        Reference(2025, 3, 20, 1403, 12, 30, Calendar.THURSDAY), // leap year: Esfand 30
        Reference(2025, 3, 21, 1404, 1, 1, Calendar.FRIDAY),
        Reference(2026, 9, 30, 1405, 7, 8, Calendar.WEDNESDAY),
        Reference(2029, 3, 20, 1408, 1, 1, Calendar.TUESDAY),
        Reference(2099, 12, 31, 1478, 10, 11, Calendar.THURSDAY)
    )

    private fun noon(y: Int, m: Int, d: Int) = Calendar.getInstance().apply {
        clear()
        set(y, m - 1, d, 12, 0)
    }

    @Test
    fun convertsReferenceDates() {
        for (r in references) {
            val actual = PersianDateUtils.fromDate(noon(r.gy, r.gm, r.gd).time)
            assertEquals("${r.gy}-${r.gm}-${r.gd}", PersianDate(r.jy, r.jm, r.jd, r.weekday), actual)
        }
    }

    @Test
    fun advancesOneJalaliDayAtATimeFrom1925To2100() {
        val day = noon(1925, 1, 1)
        var previous = PersianDateUtils.fromDate(day.time)
        while (day.get(Calendar.YEAR) <= 2100) {
            day.add(Calendar.DAY_OF_MONTH, 1)
            val current = PersianDateUtils.fromDate(day.time)
            val nextInMonth = current.month == previous.month && current.day == previous.day + 1
            val startedMonth = current.day == 1 &&
                previous.day == PersianDateUtils.daysInMonth(previous.year, previous.month)
            assertTrue("$previous -> $current", nextInMonth || startedMonth)
            previous = current
        }
    }

    @Test
    fun knowsMonthLengthsIncludingLeapEsfand() {
        assertEquals(31, PersianDateUtils.daysInMonth(1405, 1))
        assertEquals(30, PersianDateUtils.daysInMonth(1405, 7))
        assertEquals(30, PersianDateUtils.daysInMonth(1403, 12))
        assertEquals(29, PersianDateUtils.daysInMonth(1404, 12))
    }

    @Test
    fun persianDigitsKeepLeadingZeros() {
        assertEquals("۰۶:۰۵", PersianDateUtils.toPersianDigits("06:05"))
        assertEquals("۱۴۰۵", PersianDateUtils.toPersianDigits(1405))
    }
}
