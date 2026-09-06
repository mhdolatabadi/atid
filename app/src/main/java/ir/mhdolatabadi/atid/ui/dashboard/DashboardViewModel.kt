package ir.mhdolatabadi.atid.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.mhdolatabadi.atid.util.PersianDateUtils

data class CalendarMonthUiState(
    val monthLabel: String,
    val todayLabel: String,
    /** One entry per grid cell; null renders as a blank leading cell. */
    val days: List<Int?>,
    val todayDay: Int?
)

class DashboardViewModel : ViewModel() {

    private val today = PersianDateUtils.today()
    private var displayedYear = today.year
    private var displayedMonth = today.month

    private val _uiState = MutableLiveData<CalendarMonthUiState>()
    val uiState: LiveData<CalendarMonthUiState> = _uiState

    init {
        refresh()
    }

    fun goToPreviousMonth() {
        displayedMonth--
        if (displayedMonth < 1) {
            displayedMonth = 12
            displayedYear--
        }
        refresh()
    }

    fun goToNextMonth() {
        displayedMonth++
        if (displayedMonth > 12) {
            displayedMonth = 1
            displayedYear++
        }
        refresh()
    }

    private fun refresh() {
        val daysInMonth = PersianDateUtils.daysInMonth(displayedYear, displayedMonth)
        val firstWeekday = PersianDateUtils.firstDayOfWeek(displayedYear, displayedMonth)
        val leadingBlanks = PersianDateUtils.weekdayIndexSaturdayFirst(firstWeekday)

        val days = mutableListOf<Int?>()
        repeat(leadingBlanks) { days.add(null) }
        for (day in 1..daysInMonth) days.add(day)

        val isDisplayingCurrentMonth = displayedYear == today.year && displayedMonth == today.month

        _uiState.value = CalendarMonthUiState(
            monthLabel = "${PersianDateUtils.monthName(displayedMonth)} ${PersianDateUtils.toPersianDigits(displayedYear)}",
            todayLabel = "امروز: ${PersianDateUtils.weekdayName(today.dayOfWeek)} " +
                "${PersianDateUtils.toPersianDigits(today.day)} ${PersianDateUtils.monthName(today.month)} " +
                PersianDateUtils.toPersianDigits(today.year),
            days = days,
            todayDay = if (isDisplayingCurrentMonth) today.day else null
        )
    }
}
