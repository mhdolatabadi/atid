package ir.mhdolatabadi.atid.util

import ir.mhdolatabadi.atid.ui.theme.SkyPeriod
import java.util.Calendar

/** Which part of the day it is, from today's prayer times. Matches web/src/lib/sky.ts. */
object SkyPeriods {

    fun at(now: Calendar, times: PrayerTimes): SkyPeriod {
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val fajr = minutes(times.fajr)
        val sunrise = minutes(times.sunrise)
        val sunset = minutes(times.sunset)
        val maghrib = minutes(times.maghrib)
        return when {
            minute >= fajr && minute < sunrise + 20 -> SkyPeriod.DAWN
            minute >= sunrise + 20 && minute < sunset - 40 -> SkyPeriod.DAY
            minute >= sunset - 40 && minute < maghrib + 30 -> SkyPeriod.DUSK
            else -> SkyPeriod.NIGHT
        }
    }

    private fun minutes(hhmm: String): Int {
        val (h, m) = hhmm.split(":").map { it.toInt() }
        return h * 60 + m
    }
}
