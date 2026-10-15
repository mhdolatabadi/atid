package ir.mhdolatabadi.atid.ui.notifications

import ir.mhdolatabadi.atid.util.PrayerTimes
import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerCountdownTest {
    private val times = PrayerTimes(
        fajr = "04:30", sunrise = "06:00", dhuhr = "12:00", asr = "15:30",
        sunset = "18:00", maghrib = "18:20", isha = "19:30"
    )

    @Test fun countsDownToTheNextPrayerOfTheDay() {
        assertEquals("۱ ساعت تا اذان ظهر", prayerCountdown(times, 11 * 60))
        assertEquals("۴ ساعت و ۳۰ دقیقه تا اذان صبح", prayerCountdown(times, 0))
    }

    @Test fun countsDownToTomorrowsDawnAfterTheLastPrayer() {
        assertEquals("۵ ساعت و ۳۰ دقیقه تا اذان صبح فردا", prayerCountdown(times, 23 * 60))
        assertEquals("۹ ساعت تا اذان صبح فردا", prayerCountdown(times, 19 * 60 + 30))
    }
}
