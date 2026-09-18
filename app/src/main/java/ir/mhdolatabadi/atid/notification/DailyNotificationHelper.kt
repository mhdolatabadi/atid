package ir.mhdolatabadi.atid.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ir.mhdolatabadi.atid.MainActivity
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.util.PersianDateUtils
import ir.mhdolatabadi.atid.util.PrayerTimes

/**
 * Posts (and keeps up to date) an ongoing status-bar notification showing today's Jalali date
 * and shar'i prayer times, so they're visible without opening the app.
 */
object DailyNotificationHelper {

    private const val CHANNEL_ID = "daily_prayer_times"
    private const val NOTIFICATION_ID = 1001

    fun show(context: Context, times: PrayerTimes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val today = PersianDateUtils.today()
        val dateLine = "${PersianDateUtils.weekdayName(today.dayOfWeek)} " +
            "${PersianDateUtils.toPersianDigits(today.day)} ${PersianDateUtils.monthName(today.month)} " +
            PersianDateUtils.toPersianDigits(today.year)

        val summaryLine = context.getString(
            R.string.notification_summary_line,
            times.dhuhr,
            times.maghrib
        )

        val linesStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(dateLine)
            .addLine("${context.getString(R.string.label_fajr)}  ${times.fajr}")
            .addLine("${context.getString(R.string.label_sunrise)}  ${times.sunrise}")
            .addLine("${context.getString(R.string.label_dhuhr)}  ${times.dhuhr}")
            .addLine("${context.getString(R.string.label_asr)}  ${times.asr}")
            .addLine("${context.getString(R.string.label_sunset)}  ${times.sunset}")
            .addLine("${context.getString(R.string.label_maghrib)}  ${times.maghrib}")
            .addLine("${context.getString(R.string.label_isha)}  ${times.isha}")

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setContentTitle(dateLine)
            .setContentText(summaryLine)
            .setStyle(linesStyle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
