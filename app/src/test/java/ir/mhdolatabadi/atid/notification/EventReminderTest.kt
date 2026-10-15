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
        alarms.scheduledAlarms.toList().forEach { alarm ->
            alarm.operation?.let { (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(it) }
        }
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
    @Test fun advanceReminderReschedulesAndSurvivesRestore() {
        val repo = PersonalEventsRepository(context)
        val day = tomorrow
        repo.add(day, "جلسه", 600, true, 15)
        val id = repo.on(day).single().id
        assertEquals(EventReminderScheduler.triggerAt(day, 600) - 15 * 60_000L, alarms.scheduledAlarms.single().triggerAtTime)
        repo.update(day, id, "جلسه", 600, true, 60)
        EventReminderScheduler.restore(context)
        assertEquals(1, alarms.scheduledAlarms.size)
        assertEquals(EventReminderScheduler.triggerAt(day, 600) - 60 * 60_000L, alarms.scheduledAlarms.single().triggerAtTime)
        assertEquals(60, PersonalEventsRepository(context).on(day).single().reminderLeadMinutes)
    }

    @Test fun advanceOffsetsCrossJalaliYearBoundary() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tehran"))
            assertEquals(
                EventReminderScheduler.triggerAt("1403-12-30", 23 * 60 + 10),
                EventReminderScheduler.triggerAt("1404-1-1", 10, 60)
            )
            for (lead in listOf(0, 5, 15, 30, 60)) {
                assertEquals(lead * 60_000L, EventReminderScheduler.triggerAt("1404-1-1", 10) - EventReminderScheduler.triggerAt("1404-1-1", 10, lead))
            }
        } finally { TimeZone.setDefault(original) }
    }

    @Test fun advanceDeliveryRejectsOldAtTimeBroadcastAndDeliversAtChosenLead() {
        val eventTime = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }
        val day = key(eventTime.time)
        val minute = eventTime.get(Calendar.HOUR_OF_DAY) * 60 + eventTime.get(Calendar.MINUTE)
        val repo = PersonalEventsRepository(context)
        repo.add(day, "جلسه", minute, true, 15)
        val event = repo.on(day).single()
        val intent = EventReminderScheduler.intent(context, day, event.id)
        val receiver = EventReminderReceiver()
        receiver.deliver(context, intent.putExtra("at", EventReminderScheduler.triggerAt(day, minute)))
        assertTrue(notifications.allNotifications.isEmpty())
        val at = EventReminderScheduler.triggerAt(day, minute, 15)
        receiver.deliver(context, intent.putExtra("at", at))
        assertEquals(1, notifications.allNotifications.size)
        assertEquals(at, EventReminderScheduler.delivered(context, event.id))
    }

    @Test fun legacyOffsetDefaultsToZeroAndInvalidOffsetsNeverOverwrite() {
        val repo = PersonalEventsRepository(context)
        val day = tomorrow
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit()
            .putString(day, """[{"id":"old","title":"قدیمی","minuteOfDay":600,"reminder":true}]""").commit()
        assertEquals(0, repo.on(day).single().reminderLeadMinutes)
        val original = repo.on(day)
        for (lead in listOf(-5, 1, 120)) {
            try { repo.update(day, "old", "نامعتبر", 600, true, lead); fail("Invalid lead accepted") } catch (_: IllegalArgumentException) { }
            try { EventReminderScheduler.triggerAt(day, 600, lead); fail("Invalid lead scheduled") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(original, repo.on(day))
    }

}
