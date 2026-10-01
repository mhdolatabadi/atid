package ir.mhdolatabadi.atid.ui.notifications

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import ir.mhdolatabadi.atid.ui.BottomBarClearance
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.components.rememberReducedMotion
import ir.mhdolatabadi.atid.ui.theme.Atid
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.util.PersianDateUtils
import ir.mhdolatabadi.atid.util.PrayerTimes

private data class PrayerRowSpec(val key: String, val labelRes: Int, val timeSelector: (PrayerTimes) -> String)

private val prayerRowSpecs = listOf(
    PrayerRowSpec("fajr", R.string.label_fajr) { it.fajr },
    PrayerRowSpec("sunrise", R.string.label_sunrise) { it.sunrise },
    PrayerRowSpec("dhuhr", R.string.label_dhuhr) { it.dhuhr },
    PrayerRowSpec("asr", R.string.label_asr) { it.asr },
    PrayerRowSpec("sunset", R.string.label_sunset) { it.sunset },
    PrayerRowSpec("maghrib", R.string.label_maghrib) { it.maghrib },
    PrayerRowSpec("isha", R.string.label_isha) { it.isha }
)

@Composable
fun PrayerTimesScreen(viewModel: NotificationsViewModel = viewModel()) {
    val state by viewModel.uiState.observeAsState()

    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    val current = state ?: return
    val colors = Atid.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = BottomBarClearance)
    ) {
        Text(
            text = stringResource(R.string.label_prayer_times),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = current.locationLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
        )

        Text(
            text = stringResource(R.string.label_use_my_location),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    requestLocationPermission.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                .wrapContentHeight()
                .padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .glass()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            prayerRowSpecs.forEach { spec ->
                PrayerRow(
                    label = stringResource(spec.labelRes),
                    time = PersianDateUtils.toPersianDigits(spec.timeSelector(current.times)),
                    highlighted = spec.key == current.nextPrayerKey
                )
            }
        }

        Text(
            text = "محاسبه‌ی تقریبی؛ ممکن است حدود یک دقیقه با جدول رسمی تفاوت داشته باشد.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp)
        )
    }
}

@Composable
private fun PrayerRow(label: String, time: String, highlighted: Boolean) {
    val colors = Atid.colors
    val shape = RoundedCornerShape(12.dp)
    val color = if (highlighted) colors.accent else colors.text
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (highlighted) Modifier.background(colors.accentSoft, shape).border(1.dp, colors.accent, shape)
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (highlighted) {
                // A slow pulse marks the prayer that comes next.
                val pulse = rememberInfiniteTransition(label = "nextPulse")
                val alpha by pulse.animateFloat(
                    initialValue = 1f,
                    targetValue = if (rememberReducedMotion()) 1f else 0.3f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "nextPulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(7.dp)
                        .graphicsLayer { this.alpha = alpha }
                        .background(color, CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
                color = color
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
