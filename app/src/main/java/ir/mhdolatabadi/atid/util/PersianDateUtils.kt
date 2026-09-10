package ir.mhdolatabadi.atid.util

import java.util.Calendar
import java.util.Date

/**
 * A single Jalali (Persian/Shamsi) calendar date.
 *
 * @param dayOfWeek a [java.util.Calendar] weekday constant (SUNDAY=1 .. SATURDAY=7).
 */
data class PersianDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,
    val dayOfWeek: Int
)

/**
 * Jalali calendar helpers. There's no public Persian-calendar class in the Android SDK (ICU's
 * concrete implementation isn't exposed), so conversion is done with the standard Gregorian<->
 * Jalali algorithm (Kazimierski/Borkowski, via a Julian day number), validated against the
 * `jdatetime` reference implementation across every day from 1925-2100.
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
        val calendar = Calendar.getInstance()
        calendar.time = date
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) + 1
        val gd = calendar.get(Calendar.DAY_OF_MONTH)
        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        return PersianDate(jy, jm, jd, calendar.get(Calendar.DAY_OF_WEEK))
    }

    fun monthName(month: Int): String = monthNames[(month - 1).coerceIn(0, 11)]

    fun weekdayName(dayOfWeek: Int): String = weekdayNames[dayOfWeek].orEmpty()

    fun daysInMonth(year: Int, month: Int): Int = when {
        month <= 6 -> 31
        month <= 11 -> 30
        isLeapJalaaliYear(year) -> 30
        else -> 29
    }

    /** The weekday (Calendar constant) of the 1st day of [year]/[month]. */
    fun firstDayOfWeek(year: Int, month: Int): Int {
        val (gy, gm, gd) = jalaliToGregorian(year, month, 1)
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(gy, gm - 1, gd)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    /** Converts a Calendar weekday constant to a 0..6 index with Saturday first (Iranian week). */
    fun weekdayIndexSaturdayFirst(dayOfWeek: Int): Int =
        if (dayOfWeek == Calendar.SATURDAY) 0 else dayOfWeek

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(number: Int): String =
        number.toString().map { c -> if (c.isDigit()) persianDigits[c - '0'] else c }.joinToString("")

    // --- Gregorian <-> Jalali conversion (Julian day number based) ---

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    /** Returns (rawLeapCounter, gregorianYear, marchDayOfNowruz) for Jalali year [jy]. */
    private fun jalCal(jy: Int): Triple<Int, Int, Int> {
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        var i = 1
        while (i < breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ = leapJ + (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
            i++
        }
        var n = jy - jp
        leapJ = leapJ + (n / 33) * 8 + (n % 33 + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) {
            leapJ += 1
        }
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) {
            n = n - jump + (jump / 33) * 33
        }
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return Triple(leap, gy, march)
    }

    private fun isLeapJalaaliYear(jy: Int): Boolean = jalCal(jy).first == 0

    private fun gregorianToJulianDay(gy: Int, gm: Int, gd: Int): Int {
        val d = (gy + (gm - 8) / 6 + 100100) * 1461 / 4 +
            (153 * ((gm + 9) % 12) + 2) / 5 +
            gd - 34840408
        return d - ((gy + 100100 + (gm - 8) / 6) / 100 * 3) / 4 + 752
    }

    private fun julianDayToGregorian(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += ((4 * jdn + 183187720) / 146097 * 3 / 4) * 4 - 3908
        val i = (j % 1461) / 4 * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return Triple(gy, gm, gd)
    }

    private fun jalaliToJulianDay(jy: Int, jm: Int, jd: Int): Int {
        val (_, gy, march) = jalCal(jy)
        return gregorianToJulianDay(gy, 3, march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    private fun julianDayToJalali(jdn: Int): Triple<Int, Int, Int> {
        val (gy, _, _) = julianDayToGregorian(jdn)
        var jy = gy - 621
        val (leap, _, march) = jalCal(jy)
        val jdn1f = gregorianToJulianDay(gy, 3, march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return Triple(jy, 1 + k / 31, k % 31 + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (leap == 1) k += 1
        }
        return Triple(jy, 7 + k / 30, k % 30 + 1)
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> =
        julianDayToJalali(gregorianToJulianDay(gy, gm, gd))

    private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> =
        julianDayToGregorian(jalaliToJulianDay(jy, jm, jd))
}
