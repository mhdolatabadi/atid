package ir.mhdolatabadi.atid.util

import java.util.Calendar
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val sunset: String,
    val maghrib: String,
    val isha: String
)

/**
 * Approximate shar'i prayer-time calculator based on standard low-precision solar-position
 * formulas (NOAA/Meeus equation of time and declination). Uses the "Tehran" angle convention
 * (Fajr 17.7°, Maghrib 4.5°, Isha 14°) with a Jafari/Shafii (shadow factor = 1) Asr rule, which
 * is the common convention for Iran. Results are approximate, typically within about a minute
 * of official published tables.
 */
object PrayerTimesCalculator {

    private const val FAJR_ANGLE = 17.7
    private const val MAGHRIB_ANGLE = 4.5
    private const val ISHA_ANGLE = 14.0
    private const val SUN_EDGE_ANGLE = 0.833 // sunrise/sunset: refraction + solar radius
    private const val ASR_SHADOW_FACTOR = 1.0

    fun calculateForToday(latitude: Double, longitude: Double): PrayerTimes {
        val calendar = Calendar.getInstance()
        val timeZoneOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3_600_000.0
        return calculate(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
            latitude = latitude,
            longitude = longitude,
            timeZoneOffsetHours = timeZoneOffsetHours
        )
    }

    fun calculate(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZoneOffsetHours: Double
    ): PrayerTimes {
        val julianDate = julianDay(year, month, day) + 0.5
        val julianCenturies = (julianDate - 2451545.0) / 36525.0
        val (declination, equationOfTimeMinutes) = sunPosition(julianCenturies)

        val dhuhr = 12.0 - longitude / 15.0 - equationOfTimeMinutes / 60.0 + timeZoneOffsetHours
        val fajr = dhuhr - hourAngle(-FAJR_ANGLE, latitude, declination) / 15.0
        val sunrise = dhuhr - hourAngle(-SUN_EDGE_ANGLE, latitude, declination) / 15.0
        val asrAltitude = Math.toDegrees(
            atan(1.0 / (ASR_SHADOW_FACTOR + tan(Math.toRadians(abs(latitude - declination)))))
        )
        val asr = dhuhr + hourAngle(asrAltitude, latitude, declination) / 15.0
        val sunset = dhuhr + hourAngle(-SUN_EDGE_ANGLE, latitude, declination) / 15.0
        val maghrib = dhuhr + hourAngle(-MAGHRIB_ANGLE, latitude, declination) / 15.0
        val isha = dhuhr + hourAngle(-ISHA_ANGLE, latitude, declination) / 15.0

        return PrayerTimes(
            fajr = formatHours(fajr),
            sunrise = formatHours(sunrise),
            dhuhr = formatHours(dhuhr),
            asr = formatHours(asr),
            sunset = formatHours(sunset),
            maghrib = formatHours(maghrib),
            isha = formatHours(isha)
        )
    }

    /** Hour angle (degrees) at which the sun reaches [angleDeg] altitude for the given location. */
    private fun hourAngle(angleDeg: Double, latitudeDeg: Double, declinationDeg: Double): Double {
        val lat = Math.toRadians(latitudeDeg)
        val decl = Math.toRadians(declinationDeg)
        val angle = Math.toRadians(angleDeg)
        val cosH = (sin(angle) - sin(lat) * sin(decl)) / (cos(lat) * cos(decl))
        return Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0)))
    }

    /** Returns (declination in degrees, equation of time in minutes) for Julian century [t]. */
    private fun sunPosition(t: Double): Pair<Double, Double> {
        val l0 = normalizeDegrees(280.46646 + t * (36000.76983 + t * 0.0003032))
        val m = normalizeDegrees(357.52911 + t * (35999.05029 - 0.0001537 * t))
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val mRad = Math.toRadians(m)

        val centerCorrection = (1.914602 - t * (0.004817 + 0.000014 * t)) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)

        val trueLongitude = l0 + centerCorrection
        val omega = 125.04 - 1934.136 * t
        val apparentLongitude = trueLongitude - 0.00569 - 0.00478 * sin(Math.toRadians(omega))

        val meanObliquity = 23.0 +
            (26.0 + (21.448 - t * (46.8150 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
        val obliquity = meanObliquity + 0.00256 * cos(Math.toRadians(omega))

        val declination = Math.toDegrees(
            asin(sin(Math.toRadians(obliquity)) * sin(Math.toRadians(apparentLongitude)))
        )

        val y = tan(Math.toRadians(obliquity / 2.0)).pow(2)
        val l0Rad = Math.toRadians(l0)
        val equationOfTime = 4.0 * Math.toDegrees(
            y * sin(2 * l0Rad) - 2 * e * sin(mRad) + 4 * e * y * sin(mRad) * cos(2 * l0Rad) -
                0.5 * y * y * sin(4 * l0Rad) - 1.25 * e * e * sin(2 * mRad)
        )

        return declination to equationOfTime
    }

    private fun normalizeDegrees(value: Double): Double {
        val result = value % 360.0
        return if (result < 0) result + 360.0 else result
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun formatHours(hours: Double): String {
        var h = hours % 24.0
        if (h < 0) h += 24.0
        val totalMinutes = (h * 60.0).roundToInt()
        val hh = (totalMinutes / 60) % 24
        val mm = totalMinutes % 60
        return "%02d:%02d".format(hh, mm)
    }
}
