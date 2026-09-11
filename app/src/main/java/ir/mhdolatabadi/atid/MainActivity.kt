package ir.mhdolatabadi.atid

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ir.mhdolatabadi.atid.databinding.ActivityMainBinding
import ir.mhdolatabadi.atid.notification.DailyNotificationHelper
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.PrayerTimesCalculator
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,
                R.id.navigation_texts))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

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
