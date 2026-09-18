package ir.mhdolatabadi.atid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = White,
    primaryContainer = BrandPrimaryLight,
    onPrimaryContainer = OnSurfaceLight,
    secondary = BrandAccent,
    onSecondary = White,
    secondaryContainer = BrandAccent,
    onSecondaryContainer = White,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = OnSurfaceMutedLight,
    outline = DividerLight,
    error = FridayText,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = OnSurfaceDark,
    primaryContainer = BrandPrimaryDark,
    onPrimaryContainer = OnSurfaceDark,
    secondary = BrandAccent,
    onSecondary = OnSurfaceDark,
    secondaryContainer = BrandAccentDark,
    onSecondaryContainer = OnSurfaceDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceMutedDark,
    outline = DividerDark,
    error = FridayText,
    onError = White
)

@Composable
fun AtidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AtidTypography,
        content = content
    )
}
