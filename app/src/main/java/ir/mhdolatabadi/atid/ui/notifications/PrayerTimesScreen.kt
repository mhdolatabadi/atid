package ir.mhdolatabadi.atid.ui.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.mhdolatabadi.atid.notification.PrayerAlertScheduler
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
import ir.mhdolatabadi.atid.data.Cities
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        viewModel.useDeviceLocation()
    }

    val context = LocalContext.current
    var pendingAlert by remember { mutableStateOf<String?>(null) }
    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // The reminder stays on either way; the screen then says if notifications are blocked.
        pendingAlert?.let { viewModel.setAlert(it, true) }
        pendingAlert = null
    }

    // Permissions can change in system settings while the app is in the background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val current = state ?: return
    val colors = Atid.colors
    val onAlertChange = { key: String, on: Boolean ->
        if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingAlert = key
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setAlert(key, on)
        }
    }

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CityPicker(selectedSlug = current.citySlug, onSelect = viewModel::selectCity)
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
                    .padding(horizontal = 8.dp)
            )
        }

        current.countdown?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
            )
        }

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
                    highlighted = spec.key == current.nextPrayerKey,
                    alertOn = if (spec.key in PrayerAlertScheduler.PRAYERS) spec.key in current.alerts else null,
                    onAlertChange = { on -> onAlertChange(spec.key, on) }
                )
            }
        }

        AlertHint(current, context)

        Text(
            text = "محاسبه‌ی تقریبی به روش تهران؛ ممکن است حدود یک دقیقه با جدول رسمی تفاوت داشته باشد.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp)
        )
    }
}

@Composable
private fun CityPicker(selectedSlug: String?, onSelect: (String) -> Unit) {
    val colors = Atid.colors
    var expanded by remember { mutableStateOf(false) }
    val label = Cities.bySlug(selectedSlug)?.name ?: "موقعیت فعلی"
    Box {
        Text(
            text = "شهر: $label ▾",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .glass(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .wrapContentHeight()
                .padding(horizontal = 14.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Cities.all.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = {
                        expanded = false
                        onSelect(city.slug)
                    }
                )
            }
        }
    }
}

/** One line under the list: how the bells work, or what still stops them from ringing on time. */
@Composable
private fun AlertHint(state: PrayerTimesUiState, context: Context) {
    val colors = Atid.colors
    val (text, settings) = when {
        state.alerts.isEmpty() -> stringResource(R.string.prayer_alert_hint) to null
        !state.notificationsAllowed -> stringResource(R.string.prayer_alert_blocked_hint) to notificationSettingsIntent(context)
        !state.exactAlarmsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            stringResource(R.string.prayer_alert_exact_hint) to
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
        else -> return
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (settings != null) FontWeight.SemiBold else FontWeight.Normal,
        color = if (settings != null) colors.accent else colors.muted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .then(
                if (settings != null) {
                    Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { runCatching { context.startActivity(settings) } }
                        .wrapContentHeight()
                } else Modifier
            )
            .padding(horizontal = 4.dp)
    )
}

private fun notificationSettingsIntent(context: Context): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    }

@Composable
private fun PrayerRow(
    label: String,
    time: String,
    highlighted: Boolean,
    alertOn: Boolean?,
    onAlertChange: (Boolean) -> Unit
) {
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
            .heightIn(min = 52.dp)
            .padding(start = 14.dp, end = 2.dp),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
                color = color
            )
            if (alertOn != null) {
                IconToggleButton(
                    checked = alertOn,
                    onCheckedChange = onAlertChange,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(if (alertOn) R.drawable.ic_bell else R.drawable.ic_bell_outline),
                        contentDescription = stringResource(
                            if (alertOn) R.string.prayer_alert_on else R.string.prayer_alert_off, label
                        ),
                        tint = if (alertOn) colors.accent else colors.muted
                    )
                }
            } else {
                // Sunrise and sunset have no adhan; the spacer keeps every time in one column.
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}
