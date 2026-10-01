package ir.mhdolatabadi.atid.data

import ir.mhdolatabadi.atid.util.HijriDate
import ir.mhdolatabadi.atid.util.HijriDates
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar
import java.util.Date

enum class OccasionCalendar { SOLAR, LUNAR, GREGORIAN }

data class Occasion(val title: String, val holiday: Boolean, val calendar: OccasionCalendar)

/**
 * Official public holidays of Iran plus a few widely marked occasions. A 1:1 mirror of
 * web/src/data/occasions.ts: change both together, and both test suites with them.
 */
object Occasions {

    private data class Entry(val month: Int, val day: Int, val title: String, val holiday: Boolean)

    private val solar = listOf(
        Entry(1, 1, "جشن نوروز", true),
        Entry(1, 2, "عید نوروز", true),
        Entry(1, 3, "عید نوروز", true),
        Entry(1, 4, "عید نوروز", true),
        Entry(1, 12, "روز جمهوری اسلامی", true),
        Entry(1, 13, "روز طبیعت", true),
        Entry(2, 12, "روز معلم", false),
        Entry(2, 25, "روز بزرگداشت فردوسی", false),
        Entry(3, 14, "رحلت امام خمینی", true),
        Entry(3, 15, "قیام ۱۵ خرداد", true),
        Entry(5, 14, "صدور فرمان مشروطیت", false),
        Entry(7, 1, "آغاز سال تحصیلی", false),
        Entry(7, 8, "روز بزرگداشت مولوی", false),
        Entry(8, 13, "روز دانش‌آموز", false),
        Entry(9, 16, "روز دانشجو", false),
        Entry(9, 30, "شب یلدا", false),
        Entry(11, 22, "پیروزی انقلاب اسلامی", true),
        Entry(12, 15, "روز درختکاری", false),
        Entry(12, 29, "روز ملی شدن صنعت نفت", true)
    )

    private val lunar = listOf(
        Entry(1, 1, "آغاز سال هجری قمری", false),
        Entry(1, 9, "تاسوعای حسینی", true),
        Entry(1, 10, "عاشورای حسینی", true),
        Entry(2, 20, "اربعین حسینی", true),
        Entry(2, 28, "رحلت حضرت رسول اکرم و شهادت امام حسن مجتبی", true),
        Entry(3, 8, "شهادت امام حسن عسکری", true),
        Entry(3, 17, "میلاد حضرت رسول اکرم و امام جعفر صادق", true),
        Entry(6, 3, "شهادت حضرت فاطمه زهرا", true),
        Entry(6, 20, "ولادت حضرت فاطمه زهرا و روز مادر", false),
        Entry(7, 13, "ولادت امام علی و روز پدر", true),
        Entry(7, 27, "مبعث حضرت رسول اکرم", true),
        Entry(8, 3, "ولادت امام حسین", false),
        Entry(8, 15, "ولادت حضرت قائم", true),
        Entry(9, 21, "شهادت امام علی", true),
        Entry(10, 1, "عید سعید فطر", true),
        Entry(10, 2, "تعطیل به مناسبت عید سعید فطر", true),
        Entry(10, 25, "شهادت امام جعفر صادق", true),
        Entry(11, 11, "ولادت امام رضا", false),
        Entry(12, 9, "روز عرفه", false),
        Entry(12, 10, "عید سعید قربان", true),
        Entry(12, 18, "عید سعید غدیر خم", true)
    )

    private val gregorian = listOf(
        Entry(1, 1, "آغاز سال نو میلادی", false),
        Entry(12, 25, "میلاد حضرت عیسی مسیح", false)
    )

    private fun match(entries: List<Entry>, month: Int, day: Int, calendar: OccasionCalendar) =
        entries.filter { it.month == month && it.day == day }.map { Occasion(it.title, it.holiday, calendar) }

    /** [hijriOf] is injectable so JVM unit tests can run without android.icu. */
    fun on(date: Date, hijriOf: (Date) -> HijriDate = HijriDates::of): List<Occasion> {
        val solarDate = PersianDateUtils.fromDate(date)
        val hijri = hijriOf(date)
        val result = mutableListOf<Occasion>()
        result += match(solar, solarDate.month, solarDate.day, OccasionCalendar.SOLAR)
        result += match(lunar, hijri.month, hijri.day, OccasionCalendar.LUNAR)
        // Imam Reza's martyrdom is marked on the last day of Safar, whether it has 29 or 30 days.
        val tomorrow = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, 1) }.time
        if (hijri.month == 2 && hijriOf(tomorrow).month == 3) {
            result += Occasion("شهادت امام رضا", true, OccasionCalendar.LUNAR)
        }
        val calendar = Calendar.getInstance().apply { time = date }
        result += match(gregorian, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), OccasionCalendar.GREGORIAN)
        return result
    }

    /** Fridays and official holidays. */
    fun isHoliday(date: Date, hijriOf: (Date) -> HijriDate = HijriDates::of): Boolean {
        val calendar = Calendar.getInstance().apply { time = date }
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY || on(date, hijriOf).any { it.holiday }
    }
}
