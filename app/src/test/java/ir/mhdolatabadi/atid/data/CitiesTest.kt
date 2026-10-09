package ir.mhdolatabadi.atid.data

import ir.mhdolatabadi.atid.util.PrayerTimes
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Same cases as web/src/data/__tests__/cities.test.ts and the Iran-time case in prayerTimes.test.ts. */
class CitiesTest {

    @Test
    fun listsThe31ProvincialCapitals() {
        assertEquals(31, Cities.all.size)
        assertEquals(31, Cities.all.map { it.slug }.toSet().size)
        assertEquals(31, Cities.all.map { it.name }.toSet().size)
        Cities.all.forEach { assertTrue(it.slug, it.slug.matches(Regex("^[a-z]+(-[a-z]+)*$"))) }
    }

    @Test
    fun placesEveryCityInsideIran() {
        Cities.all.forEach {
            assertTrue(it.slug, it.latitude in 25.0..40.0 && it.longitude in 44.0..63.5)
        }
    }

    @Test
    fun defaultsToTehranAndFindsBySlug() {
        assertEquals("tehran", Cities.default.slug)
        assertEquals("مشهد", Cities.bySlug("mashhad")?.name)
        assertNull(Cities.bySlug("nowhere"))
    }

    @Test
    fun computesCityTimesOnIranTime() {
        val instant = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, 8, 30, 8, 0) }.timeInMillis
        val tehran = Cities.default
        assertEquals(
            PrayerTimes("04:35", "05:59", "11:54", "15:17", "17:50", "18:08", "18:55"),
            PrayerTimesCalculator.calculateForTodayInIran(tehran.latitude, tehran.longitude, instant)
        )
    }
}
