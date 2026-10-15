package ir.mhdolatabadi.atid.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.util.PersianDateUtils
import ir.mhdolatabadi.atid.util.PrayerTimes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DailyNotificationHelperTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun dailyNotificationCarriesThePortraitAndKeepsDateAndTimes() {
        val times = PrayerTimes(
            fajr = "04:30", sunrise = "06:00", dhuhr = "12:00", asr = "15:30",
            sunset = "18:00", maghrib = "18:20", isha = "19:30"
        )
        DailyNotificationHelper.show(context, times)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = shadowOf(manager).allNotifications.single()
        assertNotNull("daily notification carries the Saatbashi portrait", notification.getLargeIcon())
        assertEquals(ContextCompat.getColor(context, R.color.brand_primary), notification.color)
        val today = PersianDateUtils.today()
        val title = notification.extras.getString(Notification.EXTRA_TITLE).orEmpty()
        assertTrue(title, title.contains(PersianDateUtils.monthName(today.month)))
        assertEquals(
            context.getString(R.string.notification_summary_line, "12:00", "18:20"),
            notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertNotNull(notification.contentIntent)
    }
}
