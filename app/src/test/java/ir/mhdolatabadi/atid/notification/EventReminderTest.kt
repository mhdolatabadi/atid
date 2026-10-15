package ir.mhdolatabadi.atid.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import ir.mhdolatabadi.atid.data.PersonalEventsRepository
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class EventReminderTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val alarms get() = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    private val notifications get() = shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    private fun key(date: Date): String {
        val solar = PersianDateUtils.fromDate(date)
        return "${solar.year}-${solar.month}-${solar.day}"
    }
    private val tomorrow get() = key(Date(System.currentTimeMillis() + 86400000L))

    @Before fun clear() {
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun createsDistinctAlarmsReschedulesEditsAndCancelsDeletedOrDisabledEvents() {
        val repo = PersonalEventsRepository(context)
        val day = tomorrow
        repo.add(day, "اول", 600, true)
        repo.add(day, "دوم", 601, true)
        assertEquals(2, alarms.scheduledAlarms.size)
        assertNotEquals(alarms.scheduledAlarms[0].operation, alarms.scheduledAlarms[1].operation)
        val first = repo.on(day).first()
        repo.update(day, first.id, "جدید", 700, true)
        assertEquals(2, alarms.scheduledAlarms.size)
        assertTrue(alarms.scheduledAlarms.any { it.triggerAtTime == EventReminderScheduler.triggerAt(day, 700) })
        repo.delete(day, first.id)
        assertEquals(1, alarms.scheduledAlarms.size)
        val remaining = repo.on(day).single()
        repo.update(day, remaining.id, remaining.title, 601, false)
        assertTrue(alarms.scheduledAlarms.isEmpty())
    }

    @Test fun restoreRebuildsFutureAlarmsWithoutDuplicatesAndIgnoresBadDay() {
        val repo = PersonalEventsRepository(context)
        repo.add(tomorrow, "فردا", 600, true)
        alarms.scheduledAlarms.toList().forEach { (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(it.operation) }
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().putString("1400-1-1", "broken").commit()
        EventReminderScheduler.restore(context)
        EventReminderScheduler.restore(context)
        assertEquals(1, alarms.scheduledAlarms.size)
    }

    @Test fun deliveryReadsLatestStateAndSuppressesDuplicatesAndDeletedEvents() {
        val now = Calendar.getInstance()
        val day = key(now.time)
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val repo = PersonalEventsRepository(context)
        repo.add(day, "عنوان", minute, true)
        val event = repo.on(day).single()
        val at = EventReminderScheduler.triggerAt(day, minute)
        val intent = EventReminderScheduler.intent(context, day, event.id).putExtra("at", at)
        val receiver = EventReminderReceiver()
        receiver.deliver(context, intent)
        assertEquals(1, notifications.allNotifications.size)
        receiver.deliver(context, intent)
        assertEquals(at, EventReminderScheduler.delivered(context, event.id))
        repo.delete(day, event.id)
        receiver.deliver(context, intent)
        assertTrue(notifications.allNotifications.isEmpty())
    }

    @Test fun staleAndFutureBroadcastsDoNotDeliver() {
        val repo = PersonalEventsRepository(context)
        repo.add(tomorrow, "فردا", 600, true)
        val event = repo.on(tomorrow).single()
        val intent = EventReminderScheduler.intent(context, tomorrow, event.id)
        val receiver = EventReminderReceiver()
        receiver.deliver(context, intent.putExtra("at", 1L))
        receiver.deliver(context, intent.putExtra("at", EventReminderScheduler.triggerAt(tomorrow, 600)))
        assertTrue(notifications.allNotifications.isEmpty())
    }

    @Test fun deniedNotificationsDoNotPostOrRecordDelivery() {
        notifications.setNotificationsEnabled(false)
        val now = Calendar.getInstance()
        val day = key(now.time)
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val repo = PersonalEventsRepository(context)
        repo.add(day, "جلسه", minute, true)
        val event = repo.on(day).single()
        EventReminderReceiver().deliver(context, EventReminderScheduler.intent(context, day, event.id).putExtra("at", EventReminderScheduler.triggerAt(day, minute)))
        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(-1L, EventReminderScheduler.delivered(context, event.id))
    }

    @Test fun legacyEntriesDefaultOffAndAllDayRemindersAreRejected() {
        val repo = PersonalEventsRepository(context)
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().putString(tomorrow, """[{"id":"legacy","title":"قدیمی","minuteOfDay":600}]""").commit()
        assertFalse(repo.on(tomorrow).single().reminder)
        try { repo.add(tomorrow, "نامعتبر", null, true); fail("All-day reminder accepted") } catch (_: IllegalArgumentException) { }
        assertEquals(1, repo.on(tomorrow).size)
        assertTrue(alarms.scheduledAlarms.isEmpty())
    }

    @Test fun triggerTracksLocalTimezoneWithoutChangingCivilDate() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tehran"))
            val tehran = EventReminderScheduler.triggerAt("1405-7-23", 600)
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val utc = EventReminderScheduler.triggerAt("1405-7-23", 600)
            assertEquals(210 * 60 * 1000L, utc - tehran)
        } finally { TimeZone.setDefault(original) }
    }
}
