package ir.mhdolatabadi.atid

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ir.mhdolatabadi.atid.notification.DailyNotificationHelper
import ir.mhdolatabadi.atid.ui.AtidApp
import ir.mhdolatabadi.atid.ui.theme.AtidTheme
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        postDailyNotification()
    }

    // The whole app is Persian-only content, so force RTL layout regardless of the device's
    // system language -- otherwise a device set to English renders everything mirrored (button
    // order, icon placement) since layout direction normally follows the system locale, not the
    // text content.
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("fa")
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AtidTheme {
                AtidApp()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            postDailyNotification()
        }
    }

    private fun postDailyNotification() {
        val coordinates = LocationUtils.resolve(this)
        val times = PrayerTimesCalculator.calculateForToday(coordinates.latitude, coordinates.longitude)
        DailyNotificationHelper.show(this, times)
    }
}
