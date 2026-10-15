package ir.mhdolatabadi.atid.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.PrayerTimes
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Calendar
import java.util.TimeZone

/**
 * Schedules the adhan reminders: one alarm for the next time of each prayer the user switched on,
 * plus an alarm just after midnight that refreshes the daily notification and moves on to the next day.
 * Everything is computed and stored on the device.
 */
object PrayerAlertScheduler {
    const val ACTION_ALERT = "ir.mhdolatabadi.atid.PRAYER_ALERT"
    const val ACTION_DAY_CHANGE = "ir.mhdolatabadi.atid.DAY_CHANGE"
    const val CHANNEL = "prayer_alerts"
    private const val PREFS = "prayer_alerts"

    /** The prayers that can announce their time, in the order of the day. */
    val PRAYERS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    /** Iran time for a chosen city, as on the prayer-times screen. */
    val IRAN_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT+03:30")

    fun labelRes(key: String): Int = when (key) {
        "fajr" -> R.string.label_fajr
        "dhuhr" -> R.string.label_dhuhr
        "asr" -> R.string.label_asr
        "maghrib" -> R.string.label_maghrib
        "isha" -> R.string.label_isha
        else -> throw IllegalArgumentException(key)
    }

    fun timeOf(times: PrayerTimes, key: String): String = when (key) {
        "fajr" -> times.fajr
        "dhuhr" -> times.dhuhr
        "asr" -> times.asr
        "maghrib" -> times.maghrib
        "isha" -> times.isha
        else -> throw IllegalArgumentException(key)
    }

    /** All reminders start off; the user turns each one on. */
    fun isEnabled(context: Context, key: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, false)

    fun enabled(context: Context): Set<String> = PRAYERS.filter { isEnabled(context, it) }.toSet()

    fun setEnabled(context: Context, key: String, on: Boolean) {
        require(key in PRAYERS)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(key, on).commit()
        reschedule(context)
    }

    /** Whether alarms can ring on the minute; otherwise Android may deliver them a few minutes late. */
    fun exactAlarmsAllowed(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    /** The clock the place's times are on: Iran time for a city, the device clock for its own position. */
    fun zoneFor(coordinates: LocationUtils.Coordinates): TimeZone =
        if (coordinates.city != null) IRAN_TIME_ZONE else TimeZone.getDefault()

    /** The first time of prayer [key] after [nowMillis], today or tomorrow, at a place whose clock is [zone]. */
    fun nextOccurrence(key: String, latitude: Double, longitude: Double, zone: TimeZone, nowMillis: Long): Long {
        for (dayOffset in 0..2) {
            val day = Calendar.getInstance(zone).apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }
            val times = PrayerTimesCalculator.calculate(
                year = day.get(Calendar.YEAR),
                month = day.get(Calendar.MONTH) + 1,
                day = day.get(Calendar.DAY_OF_MONTH),
                latitude = latitude,
                longitude = longitude,
                timeZoneOffsetHours = zone.getOffset(day.timeInMillis) / 3_600_000.0
            )
            val (hour, minute) = timeOf(times, key).split(":").map { it.toInt() }
            val at = day.apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            if (at > nowMillis) return at
        }
        throw IllegalStateException("no $key time in the next days")
    }

    /** One minute past the next midnight of [zone]. */
    fun nextDayChange(zone: TimeZone, nowMillis: Long): Long =
        Calendar.getInstance(zone).apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    internal fun alertIntent(context: Context, key: String) =
        Intent(context, PrayerAlertReceiver::class.java).setAction(ACTION_ALERT)
            .setData(Uri.Builder().scheme("saatbashi").authority("prayer").appendPath(key).build())
            .putExtra("key", key)

    internal fun dayChangeIntent(context: Context) =
        Intent(context, PrayerAlertReceiver::class.java).setAction(ACTION_DAY_CHANGE)

    /** Replaces every prayer alarm with the next one for the current place and choices. */
    @Synchronized
    fun reschedule(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val coordinates = LocationUtils.resolve(context)
        val zone = zoneFor(coordinates)
        PRAYERS.forEach { key ->
            PendingIntent.getBroadcast(context, 0, alertIntent(context, key), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                ?.let { alarms.cancel(it); it.cancel() }
            if (!isEnabled(context, key)) return@forEach
            val at = nextOccurrence(key, coordinates.latitude, coordinates.longitude, zone, nowMillis)
            val token = PendingIntent.getBroadcast(
                context, 0, alertIntent(context, key).putExtra("at", at),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setAlarm(context, alarms, at, token)
        }

        // The date in the daily notification changes at midnight, on the device clock or on Iran time.
        val dayChange = minOf(nextDayChange(zone, nowMillis), nextDayChange(TimeZone.getDefault(), nowMillis))
        val token = PendingIntent.getBroadcast(
            context, 0, dayChangeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dayChange, token)
    }

    private fun setAlarm(context: Context, alarms: AlarmManager, at: Long, token: PendingIntent) {
        if (exactAlarmsAllowed(context)) {
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, token)
                return
            } catch (_: SecurityException) {
                // The exact-alarm permission can be withdrawn at any moment; fall back to an inexact alarm.
            }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, token)
    }

    internal fun delivered(context: Context, key: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("_delivered_$key", -1)

    internal fun markDelivered(context: Context, key: String, at: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("_delivered_$key", at).commit()
    }
}
