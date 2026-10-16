package ir.mhdolatabadi.atid.notification

import android.app.AlarmManager
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
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar
import java.util.concurrent.Executors

/** Announces a prayer's time, refreshes the daily notification at midnight, and restores the alarms. */
class PrayerAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        executor.execute {
            try {
                when (intent.action) {
                    PrayerAlertScheduler.ACTION_ALERT -> runCatching { deliver(context, intent) }
                    PrayerAlertScheduler.ACTION_DAY_CHANGE, in restoreActions -> runCatching { refreshDaily(context) }
                    else -> return@execute
                }
                PrayerAlertScheduler.reschedule(context)
            } finally { pending.finish() }
        }
    }

    internal fun deliver(context: Context, intent: Intent, nowMillis: Long = System.currentTimeMillis()) {
        val key = intent.getStringExtra("key") ?: return
        val at = intent.getLongExtra("at", -1)
        if (key !in PrayerAlertScheduler.PRAYERS || !PrayerAlertScheduler.isEnabled(context, key)) return
        val late = nowMillis - at
        // A reminder that arrives long after the adhan (the phone was off) would only mislead.
        if (at <= 0 || late < -60_000L || late > MAX_LATE_MILLIS || PrayerAlertScheduler.delivered(context, key) == at) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(PrayerAlertScheduler.CHANNEL, context.getString(R.string.prayer_alert_channel_name), NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = context.getString(R.string.prayer_alert_channel_description) }
            )
            if (manager.getNotificationChannel(PrayerAlertScheduler.CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val coordinates = LocationUtils.resolve(context)
        val place = coordinates.city?.let { "به افق ${it.name}" } ?: "بر اساس موقعیت مکانی شما"
        val clock = Calendar.getInstance(PrayerAlertScheduler.zoneFor(coordinates)).apply { timeInMillis = at }
        val time = PersianDateUtils.toPersianDigits(
            "%02d:%02d".format(java.util.Locale.US, clock.get(Calendar.HOUR_OF_DAY), clock.get(Calendar.MINUTE))
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, PrayerAlertScheduler.CHANNEL)
            .setSmallIcon(R.drawable.ic_notification).saatbashiBranding(context)
            .setContentTitle(context.getString(PrayerAlertScheduler.labelRes(key)))
            .setContentText(context.getString(R.string.prayer_alert_text, time, place))
            .setContentIntent(open).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(at).setShowWhen(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(TAG, PrayerAlertScheduler.PRAYERS.indexOf(key), notification)
            PrayerAlertScheduler.markDelivered(context, key, at)
        } catch (_: SecurityException) { /* Permission may be revoked between the check and delivery. */ }
    }

    private fun refreshDaily(context: Context) {
        DailyNotificationHelper.show(context, LocationUtils.timesForToday(LocationUtils.resolve(context)))
    }

    companion object {
        const val TAG = "prayer"
        private const val MAX_LATE_MILLIS = 30 * 60 * 1000L
        private val executor = Executors.newSingleThreadExecutor()
        private val restoreActions = setOf(
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
    }
}
