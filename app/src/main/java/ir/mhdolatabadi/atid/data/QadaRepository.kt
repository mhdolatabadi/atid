package ir.mhdolatabadi.atid.data

import android.content.Context

/**
 * The five daily prayers that can be tracked as qada (missed and owed).
 */
enum class QadaPrayer(val prefsKey: String) {
    FAJR("qada_fajr"),
    ZOHR("qada_zohr"),
    ASR("qada_asr"),
    MAGHRIB("qada_maghrib"),
    ISHA("qada_isha")
}

/**
 * Persists the user's outstanding qada (missed) prayer and fasting counts.
 * Counts never go below zero.
 */
class QadaRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPrayerCount(prayer: QadaPrayer): Int = prefs.getInt(prayer.prefsKey, 0)

    fun incrementPrayer(prayer: QadaPrayer): Int =
        setPrayerCount(prayer, getPrayerCount(prayer) + 1)

    fun decrementPrayer(prayer: QadaPrayer): Int =
        setPrayerCount(prayer, getPrayerCount(prayer) - 1)

    fun getFastCount(): Int = prefs.getInt(KEY_FAST, 0)

    fun incrementFast(): Int = setFastCount(getFastCount() + 1)

    fun decrementFast(): Int = setFastCount(getFastCount() - 1)

    private fun setPrayerCount(prayer: QadaPrayer, count: Int): Int {
        val clamped = count.coerceAtLeast(0)
        prefs.edit().putInt(prayer.prefsKey, clamped).apply()
        return clamped
    }

    private fun setFastCount(count: Int): Int {
        val clamped = count.coerceAtLeast(0)
        prefs.edit().putInt(KEY_FAST, clamped).apply()
        return clamped
    }

    companion object {
        private const val PREFS_NAME = "qada_prefs"
        private const val KEY_FAST = "qada_fast"
    }
}
