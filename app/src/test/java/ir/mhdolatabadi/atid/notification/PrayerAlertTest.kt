package ir.mhdolatabadi.atid.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import ir.mhdolatabadi.atid.data.Cities
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PrayerAlertTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val alarms get() = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    private val notifications get() = shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    private val tehran = Cities.default

    /** [hour]:[minute] on 1 Farvardin 1404 (21 March 2025), Iran time. */
    private fun iranMillis(hour: Int, minute: Int, day: Int = 21): Long =
        Calendar.getInstance(PrayerAlertScheduler.IRAN_TIME_ZONE).apply {
            clear()
            set(2025, Calendar.MARCH, day, hour, minute)
        }.timeInMillis

    private fun minutesOf(hhmm: String): Pair<Int, Int> = hhmm.split(":").map { it.toInt() }.let { it[0] to it[1] }

    @Before fun clear() {
        context.getSharedPreferences("prayer_alerts", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("place", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun nextOccurrenceIsTodayOnIranTimeAndRollsOverToTomorrow() {
        val now = iranMillis(10, 0)
        val today = PrayerTimesCalculator.calculate(2025, 3, 21, tehran.latitude, tehran.longitude, 3.5)
        val tomorrow = PrayerTimesCalculator.calculate(2025, 3, 22, tehran.latitude, tehran.longitude, 3.5)

        val dhuhr = minutesOf(today.dhuhr)
        assertEquals(
            iranMillis(dhuhr.first, dhuhr.second),
            PrayerAlertScheduler.nextOccurrence("dhuhr", tehran.latitude, tehran.longitude, PrayerAlertScheduler.IRAN_TIME_ZONE, now)
        )
        val fajr = minutesOf(tomorrow.fajr)
        assertEquals(
            iranMillis(fajr.first, fajr.second, day = 22),
            PrayerAlertScheduler.nextOccurrence("fajr", tehran.latitude, tehran.longitude, PrayerAlertScheduler.IRAN_TIME_ZONE, now)
        )
        // Exactly at the adhan, the next one is tomorrow's.
        val atDhuhr = iranMillis(dhuhr.first, dhuhr.second)
        val next = PrayerAlertScheduler.nextOccurrence("dhuhr", tehran.latitude, tehran.longitude, PrayerAlertScheduler.IRAN_TIME_ZONE, atDhuhr)
        assertTrue(next - atDhuhr in 23 * 3_600_000L..25 * 3_600_000L)
    }

    @Test fun nextDayChangeIsJustAfterMidnight() {
        assertEquals(iranMillis(0, 1, day = 22), PrayerAlertScheduler.nextDayChange(PrayerAlertScheduler.IRAN_TIME_ZONE, iranMillis(23, 59)))
    }

    @Test fun schedulesOnlyEnabledPrayersPlusTheMidnightRefresh() {
        PrayerAlertScheduler.reschedule(context)
        assertEquals("only the midnight refresh while every reminder is off", 1, alarms.scheduledAlarms.size)

        PrayerAlertScheduler.setEnabled(context, "dhuhr", true)
        PrayerAlertScheduler.setEnabled(context, "isha", true)
        assertEquals(setOf("dhuhr", "isha"), PrayerAlertScheduler.enabled(context))
        assertEquals(3, alarms.scheduledAlarms.size)
        val now = System.currentTimeMillis()
        assertTrue(alarms.scheduledAlarms.all { it.triggerAtTime > now && it.triggerAtTime - now <= 25 * 3_600_000L })

        PrayerAlertScheduler.setEnabled(context, "dhuhr", false)
        PrayerAlertScheduler.reschedule(context)
        assertEquals("disabling cancels and rescheduling does not duplicate", 2, alarms.scheduledAlarms.size)
    }

    @Test fun deliveryAnnouncesTheAdhanOnceAndSkipsDisabledOrStaleAlarms() {
        val now = System.currentTimeMillis()
        val receiver = PrayerAlertReceiver()
        fun alarm(key: String, at: Long) = PrayerAlertScheduler.alertIntent(context, key).putExtra("at", at)

        receiver.deliver(context, alarm("dhuhr", now), now)
        assertTrue("disabled prayers stay silent", notifications.allNotifications.isEmpty())

        PrayerAlertScheduler.setEnabled(context, "dhuhr", true)
        receiver.deliver(context, alarm("dhuhr", now - 2 * 3_600_000L), now)
        assertTrue("an alarm hours late is dropped", notifications.allNotifications.isEmpty())

        receiver.deliver(context, alarm("dhuhr", now), now)
        receiver.deliver(context, alarm("dhuhr", now), now)
        val notification = notifications.allNotifications.single()
        assertEquals("اذان ظهر", notification.extras.getString(Notification.EXTRA_TITLE))
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
        assertTrue(text, text.contains("تهران") && text.contains("تقریبی"))
    }
}
