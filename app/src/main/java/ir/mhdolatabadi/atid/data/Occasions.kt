package ir.mhdolatabadi.atid.data

import ir.mhdolatabadi.atid.util.HijriDate
import ir.mhdolatabadi.atid.util.HijriDates
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar
import java.util.Date

enum class OccasionCalendar { SOLAR, LUNAR, GREGORIAN }

data class Occasion(val title: String, val holiday: Boolean, val calendar: OccasionCalendar)

/**
 * One row of the official calendar (data/occasions/iran.json, generated into OccasionsData.kt).
 * [weekday] is Saturday = 0 .. Friday = 6; [offset] shifts a weekday rule by whole days.
 */
internal data class OccasionRule(
    val calendar: OccasionCalendar,
    val rule: Rule,
    val month: Int,
    val day: Int,
    val weekday: Int,
    val nth: Int,
    val offset: Int,
    val holiday: Boolean,
    val title: String,
) {
    enum class Rule { DATE, MONTH_END, LAST_WEEKDAY, NTH_WEEKDAY }
}

/** Official occasions of Iran. A 1:1 mirror of web/src/data/occasions.ts: change both, and both test suites. */
object Occasions {

    private val rules = occasionRules.sortedBy { it.calendar.ordinal }

    private fun addDays(date: Date, days: Int): Date = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, days)
    }.time

    private fun partsOf(date: Date, calendar: OccasionCalendar, hijriOf: (Date) -> HijriDate): Pair<Int, Int> =
        when (calendar) {
            OccasionCalendar.SOLAR -> PersianDateUtils.fromDate(date).let { it.month to it.day }
            OccasionCalendar.LUNAR -> hijriOf(date).let { it.month to it.day }
            OccasionCalendar.GREGORIAN -> Calendar.getInstance().apply { time = date }
                .let { (it.get(Calendar.MONTH) + 1) to it.get(Calendar.DAY_OF_MONTH) }
        }

    /** Saturday-first weekday (the data's convention) to a Calendar.DAY_OF_WEEK constant. */
    private fun calendarWeekday(saturdayFirst: Int): Int = (saturdayFirst + 6) % 7 + 1

    private fun weekdayOf(date: Date): Int = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)

    private fun applies(r: OccasionRule, date: Date, hijriOf: (Date) -> HijriDate): Boolean = when (r.rule) {
        OccasionRule.Rule.DATE -> partsOf(date, r.calendar, hijriOf) == (r.month to r.day)
        // The month's last day, whether it has 29 or 30 days.
        OccasionRule.Rule.MONTH_END ->
            partsOf(date, r.calendar, hijriOf).first == r.month && partsOf(addDays(date, 1), r.calendar, hijriOf).first != r.month
        // e.g. Quds day (last Friday of Ramadan); offset -1 marks the eve of that weekday.
        OccasionRule.Rule.LAST_WEEKDAY -> {
            val target = addDays(date, -r.offset)
            weekdayOf(target) == calendarWeekday(r.weekday) &&
                partsOf(target, r.calendar, hijriOf).first == r.month &&
                partsOf(addDays(target, 7), r.calendar, hijriOf).first != r.month
        }
        OccasionRule.Rule.NTH_WEEKDAY -> {
            val (month, day) = partsOf(date, r.calendar, hijriOf)
            month == r.month && weekdayOf(date) == calendarWeekday(r.weekday) && (day + 6) / 7 == r.nth
        }
    }

    /** Every official occasion on this day: solar first, then lunar, then Gregorian. [hijriOf] is injectable for JVM tests. */
    fun on(date: Date, hijriOf: (Date) -> HijriDate = HijriDates::of): List<Occasion> {
        val noon = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }.time
        return rules.filter { applies(it, noon, hijriOf) }.map { Occasion(it.title, it.holiday, it.calendar) }
    }

    /** Fridays and official holidays. */
    fun isHoliday(date: Date, hijriOf: (Date) -> HijriDate = HijriDates::of): Boolean =
        weekdayOf(date) == Calendar.FRIDAY || on(date, hijriOf).any { it.holiday }
}
