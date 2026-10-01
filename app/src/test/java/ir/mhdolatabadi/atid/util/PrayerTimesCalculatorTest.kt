package ir.mhdolatabadi.atid.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Same fixed outputs as web/src/lib/__tests__/prayerTimes.test.ts: the two ports must produce
 * identical times for identical inputs.
 */
class PrayerTimesCalculatorTest {

    private val lat = 35.6892
    private val lon = 51.389
    private val utcOffset = 3.5

    private fun tehran(y: Int, m: Int, d: Int) = PrayerTimesCalculator.calculate(y, m, d, lat, lon, utcOffset)

    @Test
    fun matchesTheWebPortForTehran() {
        assertEquals(PrayerTimes("04:35", "05:59", "11:54", "15:17", "17:50", "18:08", "18:55"), tehran(2026, 9, 30))
        assertEquals(PrayerTimes("04:43", "06:06", "12:12", "15:39", "18:17", "18:35", "19:22"), tehran(2026, 3, 21))
        assertEquals(PrayerTimes("03:02", "04:49", "12:06", "15:55", "19:24", "19:45", "20:44"), tehran(2026, 6, 21))
        assertEquals(PrayerTimes("05:40", "07:10", "12:03", "14:37", "16:55", "17:15", "18:06"), tehran(2026, 12, 21))
    }

    @Test
    fun keepsTheTimesInOrderThroughTheYear() {
        fun minutes(hhmm: String) = hhmm.substring(0, 2).toInt() * 60 + hhmm.substring(3).toInt()
        for (month in 1..12) {
            val t = tehran(2026, month, 15)
            val order = listOf(t.fajr, t.sunrise, t.dhuhr, t.asr, t.sunset, t.maghrib, t.isha).map(::minutes)
            assertEquals("month $month", order.sorted(), order)
        }
    }
}
