package ir.mhdolatabadi.atid.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ir.mhdolatabadi.atid.MainActivity
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.data.PersonalEventsRepository
import java.util.concurrent.Executors

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        executor.execute {
            try {
                if (intent.action == EventReminderScheduler.ACTION) runCatching { deliver(context, intent) }
                else if (intent.action in restoreActions) EventReminderScheduler.restore(context)
            } finally { pending.finish() }
        }
    }

    internal fun deliver(context: Context, intent: Intent) {
        val day = intent.getStringExtra("day") ?: return
        val id = intent.getStringExtra("id") ?: return
        val at = intent.getLongExtra("at", -1)
        val repo = PersonalEventsRepository(context)
        val event = runCatching { repo.on(day).firstOrNull { it.id == id } }.getOrNull() ?: return
        if (!event.reminder || event.minuteOfDay == null) return
        val expected = EventReminderScheduler.triggerAt(day, event.minuteOfDay, event.reminderLeadMinutes)
        val late = System.currentTimeMillis() - at
        if (at != expected || late < 0 || late > 24 * 60 * 60 * 1000L || EventReminderScheduler.delivered(context, id) == at) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(EventReminderScheduler.CHANNEL, "یادآوری رویدادها", NotificationManager.IMPORTANCE_DEFAULT))
            if (manager.getNotificationChannel(EventReminderScheduler.CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, EventReminderScheduler.CHANNEL)
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(event.title)
            .setContentText("یادآوری ساعت‌باشی").setContentIntent(open).setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, 0, notification)
            context.getSharedPreferences("personal_events", Context.MODE_PRIVATE).edit().putLong("_delivered_$id", at).commit()
        } catch (_: SecurityException) { /* Permission may be revoked between the check and delivery. */ }
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
        private val restoreActions = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)
    }
}
