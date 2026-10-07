package ir.mhdolatabadi.atid.util

import android.icu.util.IslamicCalendar
import java.util.Calendar
import java.util.Date

/** A lunar (Hijri) date; month is 1..12. */
data class HijriDate(val year: Int, val month: Int, val day: Int)

/**
 * Lunar dates from the Umm al-Qura calendar, the same one the web version uses. Iran's official
 * lunar calendar follows moon sighting, so a computed date can be a day off; every screen that
 * shows these dates says so.
 */
object HijriDates {

    val monthNames = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    fun monthName(month: Int): String = monthNames[(month - 1).coerceIn(0, 11)]

    fun of(date: Date): HijriDate {
        // Noon keeps the conversion on the same civil day regardless of DST.
        val local = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        val islamic = IslamicCalendar().apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            timeInMillis = local.timeInMillis
        }
        return HijriDate(
            islamic.get(IslamicCalendar.YEAR),
            islamic.get(IslamicCalendar.MONTH) + 1,
            islamic.get(IslamicCalendar.DAY_OF_MONTH)
        )
    }
}
