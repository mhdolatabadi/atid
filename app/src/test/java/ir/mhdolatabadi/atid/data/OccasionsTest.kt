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
        assertTrue(titles(noon(2025, 3, 21)).contains("آغاز نوروز"))
        assertTrue(Occasions.isHoliday(noon(2025, 3, 21), hijriOf))
    }

    @Test
    fun treatsEveryFridayAsAHoliday() {
        assertTrue(Occasions.isHoliday(noon(2026, 10, 2), hijriOf)) // Friday
        assertFalse(Occasions.isHoliday(noon(2026, 10, 3), hijriOf)) // Saturday, no occasion
    }

    @Test
    fun listsNonHolidayOccasionsWithoutMakingTheDayAHoliday() {
        assertEquals(listOf("روز بزرگداشت مولوی", "روز جهانی دریانوردی", "روز جهانی ناشنوایان"), titles(noon(2026, 9, 30)))
        assertFalse(Occasions.isHoliday(noon(2026, 9, 30), hijriOf))
    }

    @Test
    fun marksImamRezaOnTheLastDayOfSafar() {
        val day = Calendar.getInstance().apply { time = noon(2025, 1, 1) }
        val end = noon(2028, 1, 1)
        var found = 0
        while (day.time.before(end)) {
            if (titles(day.time).any { it.startsWith("شهادت حضرت امام رضا") }) {
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

    @Test
    fun marksQudsDayOnTheLastFridayOfRamadan() {
        // Ramadan 1447 runs to 19 March 2026; its last Friday is 13 March.
        val title = "روز جهانی قدس (آخرین جمعهٔ رمضان)"
        assertTrue(titles(noon(2026, 3, 13)).contains(title))
        assertFalse(titles(noon(2026, 3, 6)).contains(title))
    }

    @Test
    fun marksTheEveOfTheLastWednesdayOfTheYear() {
        // The last Wednesday of Esfand 1404 is 27 Esfand (18 March 2026).
        val title = "روز تکریم همسایگان (شب آخرین چهارشنبهٔ سال)"
        assertTrue(titles(noon(2026, 3, 17)).contains(title))
        assertFalse(titles(noon(2026, 3, 10)).contains(title))
    }

    @Test
    fun marksTheSecondFridayOfMehr() {
        // 10 Mehr 1405 (2 October 2026).
        val title = "آیین مذهبی قالیشویان اردهال (دومین جمعهٔ مهر)"
        assertTrue(titles(noon(2026, 10, 2)).contains(title))
        assertFalse(titles(noon(2026, 9, 25)).contains(title))
    }

    @Test
    fun keepsTheOfficialHolidaySet() {
        // Every non-Friday holiday in solar year 1405; same list in occasions.test.ts.
        val expected = listOf(
            "2026-3-21", "2026-3-22", "2026-3-23", "2026-3-24", "2026-4-1", "2026-4-2", "2026-4-13", "2026-5-27",
            "2026-6-4", "2026-6-24", "2026-6-25", "2026-8-3", "2026-8-11", "2026-8-13", "2026-8-30", "2026-12-22",
            "2027-1-5", "2027-1-23", "2027-2-11", "2027-2-28", "2027-3-9", "2027-3-10", "2027-3-20",
        )
        val day = Calendar.getInstance().apply { time = noon(2026, 3, 21) }
        val holidays = mutableListOf<String>()
        while (!day.time.after(noon(2027, 3, 20))) {
            if (day.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY && Occasions.isHoliday(day.time, hijriOf)) {
                holidays += "${day.get(Calendar.YEAR)}-${day.get(Calendar.MONTH) + 1}-${day.get(Calendar.DAY_OF_MONTH)}"
            }
            day.add(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(expected, holidays)
    }
}
