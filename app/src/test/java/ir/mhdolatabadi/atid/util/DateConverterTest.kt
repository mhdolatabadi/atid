package ir.mhdolatabadi.atid.util

import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Same cases as web/src/lib/__tests__/dateConverter.test.ts. android.icu is not available in JVM
 * unit tests, so the JDK's Hijrah chronology (also Umm al-Qura) stands in for HijriDates.of.
 */
class DateConverterTest {

    private val hijriOf: (Date) -> HijriDate = { date ->
        val local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val hijrah = HijrahDate.from(local)
        HijriDate(hijrah.get(ChronoField.YEAR), hijrah.get(ChronoField.MONTH_OF_YEAR), hijrah.get(ChronoField.DAY_OF_MONTH))
    }

    private fun iso(date: Date?): String? = date?.let {
        val c = Calendar.getInstance().apply { time = it }
        "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    private val hijriReferences = listOf(
        "1979-02-11" to HijriDate(1399, 3, 14),
        "2000-01-01" to HijriDate(1420, 9, 24),
        "2024-03-11" to HijriDate(1445, 9, 1),
        "2024-07-07" to HijriDate(1446, 1, 1),
        "2025-07-05" to HijriDate(1447, 1, 10),
        "2026-09-30" to HijriDate(1448, 4, 19),
        "2099-12-31" to HijriDate(1523, 10, 19),
    )

    @Test
    fun hijriReferenceDates() {
        for ((gregorian, h) in hijriReferences) {
            assertEquals("$h", gregorian, iso(DateConverter.fromHijri(h.year, h.month, h.day, hijriOf)))
        }
    }

    @Test
    fun hijriInvertsEveryDay1925To2100() {
        val mismatches = mutableListOf<String>()
        val day = Calendar.getInstance().apply { clear(); set(1925, Calendar.JANUARY, 1, 12, 0) }
        while (day.get(Calendar.YEAR) <= 2100) {
            val h = hijriOf(day.time)
            val back = DateConverter.fromHijri(h.year, h.month, h.day, hijriOf)
            if (iso(back) != iso(day.time)) mismatches += iso(day.time)!!
            day.add(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(emptyList<String>(), mismatches)
    }

    @Test
    fun rejectsDay30OfA29DayMonth() {
        // Ramadan 1445 had 30 days, Shawwal 1445 had 29 (Umm al-Qura).
        assertNotNull(DateConverter.fromHijri(1445, 9, 30, hijriOf))
        assertNull(DateConverter.fromHijri(1445, 10, 30, hijriOf))
    }

    @Test
    fun rejectsOutOfRangeHijri() {
        assertNull(DateConverter.fromHijri(1445, 13, 1, hijriOf))
        assertNull(DateConverter.fromHijri(1445, 1, 31, hijriOf))
        assertNull(DateConverter.fromHijri(1339, 1, 1, hijriOf))
    }

    @Test
    fun solarConvertsAndValidatesMonthLength() {
        assertEquals("2026-09-30", iso(DateConverter.fromSolar(1405, 7, 8)))
        assertEquals("2025-03-20", iso(DateConverter.fromSolar(1403, 12, 30))) // leap year
        assertNull(DateConverter.fromSolar(1404, 12, 30))
        assertNull(DateConverter.fromSolar(1405, 7, 31))
        assertNull(DateConverter.fromSolar(1299, 1, 1))
    }

    @Test
    fun gregorianConvertsAndValidatesMonthLength() {
        assertEquals("2024-02-29", iso(DateConverter.fromGregorian(2024, 2, 29)))
        assertNull(DateConverter.fromGregorian(2025, 2, 29))
        assertNull(DateConverter.fromGregorian(2026, 4, 31))
        assertNull(DateConverter.fromGregorian(1920, 1, 1))
    }

    @Test
    fun convertDispatchesByCalendar() {
        assertEquals("2026-09-30", iso(DateConverter.convert(CalendarKind.SOLAR, 1405, 7, 8, hijriOf)))
        assertEquals("2026-09-30", iso(DateConverter.convert(CalendarKind.GREGORIAN, 2026, 9, 30, hijriOf)))
        assertEquals("2026-09-30", iso(DateConverter.convert(CalendarKind.HIJRI, 1448, 4, 19, hijriOf)))
    }
}
