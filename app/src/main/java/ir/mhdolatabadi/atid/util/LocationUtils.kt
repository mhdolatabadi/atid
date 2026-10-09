package ir.mhdolatabadi.atid.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import ir.mhdolatabadi.atid.data.Cities
import ir.mhdolatabadi.atid.data.City

object LocationUtils {

    private const val PREFS = "place"
    private const val KEY_CITY = "city"

    /** [city] is set when the times are for a chosen (or default) city rather than the device's position. */
    data class Coordinates(val latitude: Double, val longitude: Double, val isDeviceLocation: Boolean, val city: City? = null)

    /** A city the user picked; otherwise the device's last known location when permitted; otherwise Tehran. */
    fun resolve(context: Context): Coordinates {
        Cities.bySlug(savedCitySlug(context))?.let { return it.toCoordinates() }
        val location = lastKnownLocation(context)
        return if (location != null) {
            Coordinates(location.latitude, location.longitude, isDeviceLocation = true)
        } else {
            Cities.default.toCoordinates()
        }
    }

    /** Remembers a city on this device only; null goes back to the device location. */
    fun saveCity(context: Context, slug: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_CITY, slug).apply()
    }

    fun savedCitySlug(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CITY, null)

    /** Cities use Iran time, like the web; the device position uses the device's clock. */
    fun timesForToday(coordinates: Coordinates): PrayerTimes =
        if (coordinates.city != null) {
            PrayerTimesCalculator.calculateForTodayInIran(coordinates.latitude, coordinates.longitude)
        } else {
            PrayerTimesCalculator.calculateForToday(coordinates.latitude, coordinates.longitude)
        }

    private fun City.toCoordinates() = Coordinates(latitude, longitude, isDeviceLocation = false, city = this)

    private fun lastKnownLocation(context: Context): Location? {
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
}
