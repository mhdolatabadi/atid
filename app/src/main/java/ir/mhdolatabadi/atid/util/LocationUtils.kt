package ir.mhdolatabadi.atid.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

object LocationUtils {

    // Tehran coordinates, used when location access isn't available.
    private const val DEFAULT_LATITUDE = 35.6892
    private const val DEFAULT_LONGITUDE = 51.3890

    data class Coordinates(val latitude: Double, val longitude: Double, val isDeviceLocation: Boolean)

    /** The device's last known location when permitted, otherwise Tehran's coordinates. */
    fun resolve(context: Context): Coordinates {
        val location = lastKnownLocation(context)
        return if (location != null) {
            Coordinates(location.latitude, location.longitude, isDeviceLocation = true)
        } else {
            Coordinates(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, isDeviceLocation = false)
        }
    }

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
