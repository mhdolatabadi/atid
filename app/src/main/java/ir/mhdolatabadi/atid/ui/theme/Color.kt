package ir.mhdolatabadi.atid.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The colours of the glass world. Mirrors the tokens in web/src/styles/theme.css. */
@Immutable
data class AtidColors(
    val text: Color,
    val muted: Color,
    val faint: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val holiday: Color,
    val holidaySoft: Color,
    val glass: Color,
    val glassStrong: Color,
    val glassWeak: Color,
    val glassEdge: Color,
    val glassLine: Color,
    val isDark: Boolean
)

val LightAtidColors = AtidColors(
    text = Color(0xFF121A2C),
    muted = Color(0xFF45506A),
    faint = Color(0xFF7C86A0),
    accent = Color(0xFF2448C9),
    accentSoft = Color(0x1F2448C9),
    onAccent = Color(0xFFFFFFFF),
    holiday = Color(0xFFC81F45),
    holidaySoft = Color(0x1FC81F45),
    glass = Color(0x94FFFFFF),
    glassStrong = Color(0xC7FFFFFF),
    glassWeak = Color(0x57FFFFFF),
    glassEdge = Color(0xBFFFFFFF),
    glassLine = Color(0x1A141E3C),
    isDark = false
)

val DarkAtidColors = AtidColors(
    text = Color(0xFFF2F4FB),
    muted = Color(0xFFC3C9DC),
    faint = Color(0xFF8890A8),
    accent = Color(0xFFFFD27A),
    accentSoft = Color(0x29FFD27A),
    onAccent = Color(0xFF1C1405),
    holiday = Color(0xFFFF8FA3),
    holidaySoft = Color(0x29FF8FA3),
    glass = Color(0x80101428),
    glassStrong = Color(0xB3101428),
    glassWeak = Color(0x0FFFFFFF),
    glassEdge = Color(0x24FFFFFF),
    glassLine = Color(0x1AFFFFFF),
    isDark = true
)

val LocalAtidColors = staticCompositionLocalOf { LightAtidColors }

/** Part of the day, from today's prayer times; the backdrop takes its colours from it. */
enum class SkyPeriod { DAWN, DAY, DUSK, NIGHT }

@Immutable
data class SkyPalette(val top: Color, val mid: Color, val low: Color, val orb1: Color, val orb2: Color, val orb3: Color)

fun skyPalette(period: SkyPeriod, dark: Boolean): SkyPalette = if (dark) {
    when (period) {
        SkyPeriod.NIGHT -> SkyPalette(Color(0xFF030617), Color(0xFF0B1234), Color(0xFF17224C), Color(0xFF4A3FC4), Color(0xFF1F7F8C), Color(0xFF7A3A86))
        SkyPeriod.DAWN -> SkyPalette(Color(0xFF281534), Color(0xFF693950), Color(0xFFA65D65), Color(0xFFC2587A), Color(0xFFD08A4A), Color(0xFF4D4FB8))
        SkyPeriod.DAY -> SkyPalette(Color(0xFF072C46), Color(0xFF105C78), Color(0xFF298B9C), Color(0xFF2F8FD6), Color(0xFF2BB3A6), Color(0xFF5A5AD0))
        SkyPeriod.DUSK -> SkyPalette(Color(0xFF361128), Color(0xFF853B48), Color(0xFFB56640), Color(0xFFD8664A), Color(0xFFB04C8C), Color(0xFF5A47B8))
    }
} else {
    when (period) {
        SkyPeriod.NIGHT -> SkyPalette(Color(0xFFB0B8E4), Color(0xFFA9A0D8), Color(0xFFC5BCE8), Color(0xFF8F86FF), Color(0xFF7CC6FF), Color(0xFFD59CF0))
        SkyPeriod.DAWN -> SkyPalette(Color(0xFFF2B1B0), Color(0xFFFFD1B0), Color(0xFFFFF0D2), Color(0xFFFF9FB5), Color(0xFFFFCF8F), Color(0xFFA9B6FF))
        SkyPeriod.DAY -> SkyPalette(Color(0xFF83CFFF), Color(0xFFB4E8FF), Color(0xFFE0FBFF), Color(0xFF7FC0FF), Color(0xFF9FE3E0), Color(0xFFC6C0FF))
        SkyPeriod.DUSK -> SkyPalette(Color(0xFFD49AC0), Color(0xFFF4AF97), Color(0xFFFFD79B), Color(0xFFFF9A7A), Color(0xFFF5A3C7), Color(0xFFB39CF5))
    }
}
