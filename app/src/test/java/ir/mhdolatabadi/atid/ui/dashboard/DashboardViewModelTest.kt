package ir.mhdolatabadi.atid.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ir.mhdolatabadi.atid.util.PersianDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DashboardViewModelTest {
    @get:Rule val executor = InstantTaskExecutorRule()

    @Test fun nextMonthSelectsFirstDayAcrossNewYear() {
        val model = DashboardViewModel { PersianDateUtils.toGregorianDate(1403, 12, 30) }
        model.goToNextMonth()
        val state = model.uiState.value!!
        assertEquals(1404 * 12 + 1, state.monthIndex)
        assertEquals(state.days.filterNotNull().first().date, state.selectedDate)
    }

    @Test fun previousMonthSelectsFirstDayAcrossNewYear() {
        val model = DashboardViewModel { PersianDateUtils.toGregorianDate(1404, 1, 15) }
        model.goToPreviousMonth()
        val state = model.uiState.value!!
        assertEquals(1403 * 12 + 12, state.monthIndex)
        assertEquals(state.days.filterNotNull().first().date, state.selectedDate)
    }

    @Test fun todayRefreshesAfterMidnightAndRestoresMonth() {
        var now = PersianDateUtils.toGregorianDate(1403, 12, 30)
        val model = DashboardViewModel { now }
        model.goToPreviousMonth()
        now = PersianDateUtils.toGregorianDate(1404, 1, 1)
        model.goToToday()
        val state = model.uiState.value!!
        assertEquals(1404 * 12 + 1, state.monthIndex)
        assertEquals(now, state.selectedDate)
        assertTrue(state.days.filterNotNull().first().isToday)
    }

    @Test fun daySelectionUpdatesAllDetails() {
        val model = DashboardViewModel { PersianDateUtils.toGregorianDate(1404, 1, 15) }
        val first = model.uiState.value!!.days.filterNotNull().first().date
        model.select(first)
        assertEquals(first, model.uiState.value!!.selectedDate)
        assertTrue(model.uiState.value!!.selected.solarLabel.contains("۱ فروردین"))
    }
}
