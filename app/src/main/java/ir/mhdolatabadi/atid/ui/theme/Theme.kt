package ir.mhdolatabadi.atid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private fun materialColors(c: AtidColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.accent, onPrimary = c.onAccent, primaryContainer = c.accentSoft, onPrimaryContainer = c.text,
        secondary = c.accent, onSecondary = c.onAccent,
        background = Color.Transparent, onBackground = c.text,
        surface = c.glass, onSurface = c.text, surfaceVariant = c.glassWeak, onSurfaceVariant = c.muted,
        outline = c.glassLine, error = c.holiday, onError = c.onAccent
    )
} else {
    lightColorScheme(
        primary = c.accent, onPrimary = c.onAccent, primaryContainer = c.accentSoft, onPrimaryContainer = c.text,
        secondary = c.accent, onSecondary = c.onAccent,
        background = Color.Transparent, onBackground = c.text,
        surface = c.glass, onSurface = c.text, surfaceVariant = c.glassWeak, onSurfaceVariant = c.muted,
        outline = c.glassLine, error = c.holiday, onError = c.onAccent
    )
}

@Composable
fun AtidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAtidColors else LightAtidColors
    CompositionLocalProvider(LocalAtidColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors(colors),
            typography = AtidTypography,
            content = content
        )
    }
}

/** Shorthand for the glass palette inside composables. */
object Atid {
    val colors: AtidColors
        @Composable get() = LocalAtidColors.current
}
