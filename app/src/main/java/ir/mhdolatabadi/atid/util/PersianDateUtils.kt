package ir.mhdolatabadi.atid.util

import android.icu.util.Calendar
import android.icu.util.PersianCalendar
import java.util.Date

/**
 * A single Jalali (Persian/Shamsi) calendar date.
 *
 * @param dayOfWeek an [android.icu.util.Calendar] weekday constant (SUNDAY=1 .. SATURDAY=7).
 */
data class PersianDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,
    val dayOfWeek: Int
)

/**
 * Jalali calendar helpers built on the platform's [PersianCalendar] (available since API 24,
 * which matches this app's minSdk), so no manual Gregorian<->Jalali conversion is needed.
 */
object PersianDateUtils {

    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val weekdayNames = mapOf(
        Calendar.SATURDAY to "شنبه",
        Calendar.SUNDAY to "یک‌شنبه",
        Calendar.MONDAY to "دوشنبه",
        Calendar.TUESDAY to "سه‌شنبه",
        Calendar.WEDNESDAY to "چهارشنبه",
        Calendar.THURSDAY to "پنجشنبه",
        Calendar.FRIDAY to "جمعه"
    )

    fun today(): PersianDate = fromDate(Date())

    fun fromDate(date: Date): PersianDate {
        val calendar = PersianCalendar()
        calendar.time = date
        return PersianDate(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun monthName(month: Int): String = monthNames[(month - 1).coerceIn(0, 11)]

    fun weekdayName(dayOfWeek: Int): String = weekdayNames[dayOfWeek].orEmpty()

    fun daysInMonth(year: Int, month: Int): Int = firstDayCalendar(year, month)
        .getActualMaximum(Calendar.DAY_OF_MONTH)

    /** The weekday (Calendar constant) of the 1st day of [year]/[month]. */
    fun firstDayOfWeek(year: Int, month: Int): Int =
        firstDayCalendar(year, month).get(Calendar.DAY_OF_WEEK)

    /** Converts a Calendar weekday constant to a 0..6 index with Saturday first (Iranian week). */
    fun weekdayIndexSaturdayFirst(dayOfWeek: Int): Int =
        if (dayOfWeek == Calendar.SATURDAY) 0 else dayOfWeek

    private fun firstDayCalendar(year: Int, month: Int): PersianCalendar {
        val calendar = PersianCalendar()
        calendar.clear()
        calendar.set(year, month - 1, 1)
        return calendar
    }

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(number: Int): String =
        number.toString().map { c -> if (c.isDigit()) persianDigits[c - '0'] else c }.joinToString("")
}
