package ir.mhdolatabadi.atid.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.mhdolatabadi.atid.notification.DailyNotificationHelper
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.PrayerTimes
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Calendar

data class PrayerTimesUiState(
    val times: PrayerTimes,
    val locationLabel: String,
    /** Key of the next upcoming prayer (fajr/sunrise/dhuhr/asr/sunset/maghrib/isha), for highlighting. */
    val nextPrayerKey: String
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<PrayerTimesUiState>()
    val uiState: LiveData<PrayerTimesUiState> = _uiState

    init {
        refresh()
    }

    /** Recomputes prayer times, using the device's last known location when permitted. */
    fun refresh() {
        val context = getApplication<Application>()
        val coordinates = LocationUtils.resolve(context)
        val locationLabel = if (coordinates.isDeviceLocation) {
            "بر اساس موقعیت مکانی شما"
        } else {
            "تهران (پیش‌فرض؛ دسترسی به موقعیت مکانی فعال نیست)"
        }

        val times = PrayerTimesCalculator.calculateForToday(coordinates.latitude, coordinates.longitude)
        _uiState.value = PrayerTimesUiState(
            times = times,
            locationLabel = locationLabel,
            nextPrayerKey = computeNextPrayerKey(times)
        )
        DailyNotificationHelper.show(context, times)
    }

    private fun computeNextPrayerKey(times: PrayerTimes): String {
        val ordered = listOf(
            "fajr" to times.fajr,
            "sunrise" to times.sunrise,
            "dhuhr" to times.dhuhr,
            "asr" to times.asr,
            "sunset" to times.sunset,
            "maghrib" to times.maghrib,
            "isha" to times.isha
        )
        val nowCalendar = Calendar.getInstance()
        val nowMinutes = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)
        return ordered.firstOrNull { (_, time) -> toMinutesOfDay(time) > nowMinutes }?.first
            ?: ordered.first().first
    }

    private fun toMinutesOfDay(hhmm: String): Int {
        val (hour, minute) = hhmm.split(":").map { it.toInt() }
        return hour * 60 + minute
    }
}
