package ir.mhdolatabadi.atid.util

import java.util.Calendar
import java.util.Date

/** Persian labels for a day in each calendar; mirrors web/src/lib/dateLabels.ts. */
object DateLabels {

    fun solar(date: Date): String {
        val p = PersianDateUtils.fromDate(date)
        return "${PersianDateUtils.weekdayName(p.dayOfWeek)} ${PersianDateUtils.toPersianDigits(p.day)} " +
            "${PersianDateUtils.monthName(p.month)} ${PersianDateUtils.toPersianDigits(p.year)}"
    }

    fun hijri(date: Date): String {
        val h = HijriDates.of(date)
        return "${PersianDateUtils.toPersianDigits(h.day)} ${HijriDates.monthName(h.month)} ${PersianDateUtils.toPersianDigits(h.year)}"
    }

    fun gregorian(date: Date): String {
        val c = Calendar.getInstance().apply { time = date }
        return "${PersianDateUtils.toPersianDigits(c.get(Calendar.DAY_OF_MONTH))} " +
            "${PersianDateUtils.gregorianMonthNames[c.get(Calendar.MONTH)]} ${PersianDateUtils.toPersianDigits(c.get(Calendar.YEAR))}"
    }
}
