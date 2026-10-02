package ir.mhdolatabadi.atid.util

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToLong

enum class CalendarKind { SOLAR, HIJRI, GREGORIAN }

/**
 * Turns a day/month/year typed into the converter into a civil date. Each function returns the
 * local [Date] at noon, or null when the input is not a real day in that calendar's supported
 * range. A 1:1 port of web/src/lib/dateConverter.ts.
 */
object DateConverter {

    /** Supported input years; together they cover roughly 1921–2121. */
    val yearRanges: Map<CalendarKind, IntRange> = mapOf(
        CalendarKind.SOLAR to 1300..1500,
        CalendarKind.GREGORIAN to 1921..2121,
        CalendarKind.HIJRI to 1340..1545,
    )

    private const val MS_PER_DAY = 86_400_000L
    private const val MEAN_LUNAR_YEAR = 354.367
    private const val MEAN_LUNAR_MONTH = 29.5306

    /** 1 Muharram 1 AH in the proleptic Gregorian calendar, as UTC milliseconds (Date.UTC(622, 6, 19)). */
    private const val HIJRI_EPOCH = -42521587200000L

    private fun inRange(kind: CalendarKind, year: Int, month: Int, day: Int): Boolean =
        year in yearRanges.getValue(kind) && month in 1..12 && day >= 1

    private fun localNoon(year: Int, month: Int, day: Int): Calendar = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day, 12, 0)
    }

    fun fromSolar(year: Int, month: Int, day: Int): Date? {
        if (!inRange(CalendarKind.SOLAR, year, month, day) || day > PersianDateUtils.daysInMonth(year, month)) return null
        return PersianDateUtils.toGregorianDate(year, month, day)
    }

    fun fromGregorian(year: Int, month: Int, day: Int): Date? {
        if (!inRange(CalendarKind.GREGORIAN, year, month, day)) return null
        val calendar = localNoon(year, month, day)
        return if (calendar.get(Calendar.MONTH) == month - 1) calendar.time else null
    }

    /**
     * Umm al-Qura has no closed-form inverse, so this estimates the day from mean month lengths
     * and then searches nearby days with the same [hijriOf] used everywhere else, keeping both
     * directions consistent. Day 30 of a 29-day month finds no match and is rejected.
     * [hijriOf] is injectable so JVM unit tests can run without android.icu.
     */
    fun fromHijri(year: Int, month: Int, day: Int, hijriOf: (Date) -> HijriDate = HijriDates::of): Date? {
        if (!inRange(CalendarKind.HIJRI, year, month, day) || day > 30) return null
        val days = ((year - 1) * MEAN_LUNAR_YEAR + (month - 1) * MEAN_LUNAR_MONTH + (day - 1)).roundToLong()
        val estimate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = HIJRI_EPOCH + days * MS_PER_DAY }
        val base = localNoon(estimate.get(Calendar.YEAR), estimate.get(Calendar.MONTH) + 1, estimate.get(Calendar.DAY_OF_MONTH))
        val target = HijriDate(year, month, day)
        for (offset in 0..20) {
            for (sign in if (offset == 0) intArrayOf(1) else intArrayOf(1, -1)) {
                val candidate = (base.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, sign * offset) }.time
                if (hijriOf(candidate) == target) return candidate
            }
        }
        return null
    }

    fun convert(
        kind: CalendarKind,
        year: Int,
        month: Int,
        day: Int,
        hijriOf: (Date) -> HijriDate = HijriDates::of,
    ): Date? = when (kind) {
        CalendarKind.SOLAR -> fromSolar(year, month, day)
        CalendarKind.GREGORIAN -> fromGregorian(year, month, day)
        CalendarKind.HIJRI -> fromHijri(year, month, day, hijriOf)
    }
}
