package ir.mhdolatabadi.atid.ui.notifications

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.mhdolatabadi.atid.util.PrayerTimes
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator

data class PrayerTimesUiState(
    val times: PrayerTimes,
    val locationLabel: String
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<PrayerTimesUiState>()
    val uiState: LiveData<PrayerTimesUiState> = _uiState

    init {
        refresh()
    }

    /** Recomputes prayer times, using the device's last known location when permitted. */
    fun refresh() {
        val location = lastKnownLocation()
        val latitude: Double
        val longitude: Double
        val locationLabel: String
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            locationLabel = "بر اساس موقعیت مکانی شما"
        } else {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
            locationLabel = "تهران (پیش‌فرض؛ دسترسی به موقعیت مکانی فعال نیست)"
        }

        _uiState.value = PrayerTimesUiState(
            times = PrayerTimesCalculator.calculateForToday(latitude, longitude),
            locationLabel = locationLabel
        )
    }

    private fun lastKnownLocation(): Location? {
        val context = getApplication<Application>()
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    companion object {
        // Tehran coordinates, used when location access isn't available.
        private const val DEFAULT_LATITUDE = 35.6892
        private const val DEFAULT_LONGITUDE = 51.3890
    }
}
