package ir.mhdolatabadi.atid.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.os.Build
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import ir.mhdolatabadi.atid.data.PersonalEvent
import ir.mhdolatabadi.atid.data.PersonalEventsRepository
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar

object EventReminderScheduler {
    const val ACTION = "ir.mhdolatabadi.atid.EVENT_REMINDER"
    const val CHANNEL = "personal_event_reminders"

    fun notificationsEnabled(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    fun triggerAt(day: String, minute: Int, leadMinutes: Int = 0): Long {
        require(minute in 0..1439)
        require(leadMinutes in PersonalEvent.REMINDER_LEADS)
        val parts = day.split('-').map { it.toInt() }
        require(parts.size == 3)
        val date = PersianDateUtils.toGregorianDate(parts[0], parts[1], parts[2])
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, minute / 60)
            set(Calendar.MINUTE, minute % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis - leadMinutes * 60_000L
    }

    internal fun intent(context: Context, day: String, id: String) =
        Intent(context, EventReminderReceiver::class.java).setAction(ACTION)
            .setData(Uri.Builder().scheme("saatbashi").authority("event").appendPath(day).appendPath(id).build())
            .putExtra("day", day).putExtra("id", id)

    @Synchronized
    fun replaceDay(context: Context, day: String, previous: List<PersonalEvent>, snapshot: List<PersonalEvent>) {
        val current = PersonalEventsRepository(context).on(day)
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (previous + snapshot + current).distinctBy { it.id }.forEach { event ->
            val token = PendingIntent.getBroadcast(context, 0, intent(context, day, event.id), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (token != null) { alarms.cancel(token); token.cancel() }
            NotificationManagerCompat.from(context).cancel(event.id, 0)
        }
        current.forEach { event ->
            if (!event.reminder || event.minuteOfDay == null) return@forEach
            val at = triggerAt(day, event.minuteOfDay, event.reminderLeadMinutes)
            if (at <= System.currentTimeMillis() || delivered(context, event.id) == at) return@forEach
            val token = PendingIntent.getBroadcast(context, 0, intent(context, day, event.id).putExtra("at", at), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            // Inexact by design: no special exact-alarm access is required.
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, token)
        }
    }

    fun restore(context: Context) {
        val repo = PersonalEventsRepository(context)
        repo.days().forEach { day ->
            // A damaged day must not prevent other reminders from being restored.
            runCatching { val events = repo.on(day); replaceDay(context, day, events, events) }
        }
    }

    internal fun delivered(context: Context, id: String) =
        context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).getLong("_delivered_$id", -1)
}
