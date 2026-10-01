package ir.mhdolatabadi.atid.data

import ir.mhdolatabadi.atid.util.HijriDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Same cases as web/src/data/__tests__/occasions.test.ts. android.icu is not available in JVM
 * unit tests, so the JDK's Hijrah chronology (also Umm al-Qura) stands in for HijriDates.of.
 */
class OccasionsTest {

    private val hijriOf: (Date) -> HijriDate = { date ->
        val local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val hijrah = HijrahDate.from(local)
        HijriDate(hijrah.get(ChronoField.YEAR), hijrah.get(ChronoField.MONTH_OF_YEAR), hijrah.get(ChronoField.DAY_OF_MONTH))
    }

    private fun noon(y: Int, m: Int, d: Int): Date = Calendar.getInstance().apply {
        clear()
        set(y, m - 1, d, 12, 0)
    }.time

    private fun titles(date: Date) = Occasions.on(date, hijriOf).map { it.title }

    @Test
    fun usesUmmAlQura() {
        assertEquals(HijriDate(1448, 4, 19), hijriOf(noon(2026, 9, 30)))
    }

    @Test
    fun marksNowruz() {
        assertTrue(titles(noon(2025, 3, 21)).contains("جشن نوروز"))
        assertTrue(Occasions.isHoliday(noon(2025, 3, 21), hijriOf))
    }

    @Test
    fun treatsEveryFridayAsAHoliday() {
        assertTrue(Occasions.isHoliday(noon(2026, 10, 2), hijriOf)) // Friday
        assertFalse(Occasions.isHoliday(noon(2026, 10, 3), hijriOf)) // Saturday, no occasion
    }

    @Test
    fun listsNonHolidayOccasionsWithoutMakingTheDayAHoliday() {
        assertEquals(listOf("روز بزرگداشت مولوی"), titles(noon(2026, 9, 30)))
        assertFalse(Occasions.isHoliday(noon(2026, 9, 30), hijriOf))
    }

    @Test
    fun marksImamRezaOnTheLastDayOfSafar() {
        val day = Calendar.getInstance().apply { time = noon(2025, 1, 1) }
        val end = noon(2028, 1, 1)
        var found = 0
        while (day.time.before(end)) {
            if (titles(day.time).contains("شهادت امام رضا")) {
                found++
                val next = Calendar.getInstance().apply { time = day.time; add(Calendar.DAY_OF_MONTH, 1) }.time
                assertEquals(2, hijriOf(day.time).month)
                assertEquals(3, hijriOf(next).month)
                assertEquals(1, hijriOf(next).day)
            }
            day.add(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(3, found)
    }
}
