package ir.mhdolatabadi.atid.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.mhdolatabadi.atid.ui.theme.Atid
import ir.mhdolatabadi.atid.ui.theme.SkyPeriod
import ir.mhdolatabadi.atid.ui.theme.skyPalette
import ir.mhdolatabadi.atid.util.LocationUtils
import ir.mhdolatabadi.atid.util.SkyPeriods
import kotlinx.coroutines.delay
import java.util.Calendar

val GlassShape = RoundedCornerShape(18.dp)

/** A frosted pane: translucent fill with a light edge. The sky behind it does the rest. */
fun Modifier.glass(shape: Shape = GlassShape, strong: Boolean = false): Modifier = composed {
    val c = Atid.colors
    this
        .background(if (strong) c.glassStrong else c.glass, shape)
        .border(1.dp, c.glassEdge, shape)
}

/** True when the user turned animations off (Developer options or accessibility "Remove animations"). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** Shrinks slightly while pressed, so taps get an immediate physical response. */
fun Modifier.pressScale(interactionSource: MutableInteractionSource, pressed: Float = 0.92f): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) pressed else 1f, tween(160), label = "press")
    graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
private fun rememberSkyPeriod(): SkyPeriod {
    val context = LocalContext.current
    fun compute(): SkyPeriod {
        val coordinates = LocationUtils.resolve(context)
        val times = LocationUtils.timesForToday(coordinates)
        return SkyPeriods.at(Calendar.getInstance(), times)
    }
    var period by remember { mutableStateOf(compute()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            period = compute()
        }
    }
    return period
}

/**
 * The living backdrop: a gradient whose colours follow the current prayer period and glide when it
 * changes, with three soft lights drifting slowly. Lights are radial gradients, not blurs, so this
 * stays cheap on every API level.
 */
@Composable
fun SkyBackground(modifier: Modifier = Modifier) {
    val dark = Atid.colors.isDark
    val palette = skyPalette(rememberSkyPeriod(), dark)
    val glide = tween<Color>(2400)
    val top by animateColorAsState(palette.top, glide, label = "skyTop")
    val mid by animateColorAsState(palette.mid, glide, label = "skyMid")
    val low by animateColorAsState(palette.low, glide, label = "skyLow")
    val orb1 by animateColorAsState(palette.orb1, glide, label = "orb1")
    val orb2 by animateColorAsState(palette.orb2, glide, label = "orb2")
    val orb3 by animateColorAsState(palette.orb3, glide, label = "orb3")

    val reducedMotion = rememberReducedMotion()
    val drift = rememberInfiniteTransition(label = "drift")
    val t by drift.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "driftT"
    )
    val eased = FastOutSlowInEasing.transform(t)
    val orbAlpha = if (dark) 0.75f else 0.85f

    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(top, mid, low)))
        val r = size.maxDimension * 0.6f
        fun orb(color: Color, x: Float, y: Float, radius: Float) {
            drawCircle(
                Brush.radialGradient(
                    listOf(color.copy(alpha = orbAlpha), color.copy(alpha = 0f)),
                    center = Offset(x, y),
                    radius = radius
                ),
                radius = radius,
                center = Offset(x, y)
            )
        }
        orb(orb1, size.width * (0.95f - 0.25f * eased), size.height * (0.05f + 0.12f * eased), r)
        orb(orb2, size.width * (0.05f + 0.3f * eased), size.height * (0.95f - 0.1f * eased), r)
        orb(orb3, size.width * (0.5f - 0.15f * eased), size.height * (0.5f - 0.15f * eased), r * 0.6f)
    }
}
