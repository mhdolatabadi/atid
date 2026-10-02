package ir.mhdolatabadi.atid.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.theme.Atid
import ir.mhdolatabadi.atid.util.CalendarKind
import ir.mhdolatabadi.atid.util.DateConverter
import ir.mhdolatabadi.atid.util.DateLabels
import ir.mhdolatabadi.atid.util.HijriDates
import ir.mhdolatabadi.atid.util.PersianDateUtils
import java.util.Calendar
import java.util.Date

private val modes = listOf(
    CalendarKind.SOLAR to "از شمسی",
    CalendarKind.HIJRI to "از قمری",
    CalendarKind.GREGORIAN to "از میلادی",
)

private fun todayIn(kind: CalendarKind): List<String> {
    val now = Date()
    val (y, m, d) = when (kind) {
        CalendarKind.SOLAR -> PersianDateUtils.fromDate(now).let { Triple(it.year, it.month, it.day) }
        CalendarKind.HIJRI -> HijriDates.of(now).let { Triple(it.year, it.month, it.day) }
        CalendarKind.GREGORIAN -> Calendar.getInstance().let {
            Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
        }
    }
    return listOf(d.toString(), m.toString(), y.toString())
}

/** Persian and Arabic-Indic digits typed on the keyboard become Latin digits; anything else is dropped. */
private fun normalizeDigits(text: String): String = text.mapNotNull { c ->
    when (c) {
        in '0'..'9' -> c
        in '۰'..'۹' -> '0' + (c - '۰')
        in '٠'..'٩' -> '0' + (c - '٠')
        else -> null
    }
}.joinToString("").take(4)

/** Converts a day between the solar, lunar and Gregorian calendars; mirrors the web converter. */
@Composable
fun DateConverterCard(modifier: Modifier = Modifier) {
    val colors = Atid.colors
    var mode by rememberSaveable { mutableStateOf(CalendarKind.SOLAR) }
    // day, month, year
    var fields by rememberSaveable { mutableStateOf(todayIn(CalendarKind.SOLAR)) }
    val result = remember(mode, fields) {
        val (d, m, y) = fields.map { it.toIntOrNull() ?: 0 }
        DateConverter.convert(mode, y, m, d)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
    ) {
        Text(
            text = "تبدیل تاریخ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.glassWeak, RoundedCornerShape(14.dp))
                .border(1.dp, colors.glassLine, RoundedCornerShape(14.dp))
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            modes.forEach { (kind, label) ->
                val selected = mode == kind
                val background by animateColorAsState(
                    if (selected) colors.glassStrong else colors.glassWeak.copy(alpha = 0f),
                    tween(220),
                    label = "segment"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .background(background, RoundedCornerShape(10.dp))
                        .selectable(selected = selected, role = Role.RadioButton) {
                            if (!selected) {
                                mode = kind
                                fields = todayIn(kind)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) colors.text else colors.muted
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("روز", "ماه", "سال").forEachIndexed { index, label ->
                NumberField(
                    label = label,
                    value = fields[index],
                    onValueChange = { value -> fields = fields.toMutableList().also { it[index] = normalizeDigits(value) } },
                    last = index == 2,
                    modifier = Modifier.weight(if (index == 2) 1.4f else 1f)
                )
            }
        }

        AnimatedContent(
            targetState = result,
            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
            label = "converterResult"
        ) { date ->
            if (date != null) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ResultRow("شمسی", DateLabels.solar(date))
                    Divider(color = colors.glassLine)
                    ResultRow("قمری", DateLabels.hijri(date))
                    Divider(color = colors.glassLine)
                    ResultRow("میلادی", DateLabels.gregorian(date))
                }
            } else {
                val range = DateConverter.yearRanges.getValue(mode)
                Text(
                    text = "تاریخ واردشده معتبر نیست. سال باید بین ${PersianDateUtils.toPersianDigits(range.first)} و " +
                        "${PersianDateUtils.toPersianDigits(range.last)} باشد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.holiday,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Text(
            text = "تاریخ قمری بر پایه‌ی تقویم ام‌القری محاسبه می‌شود و ممکن است با تقویم رسمی ایران (رؤیت هلال) یک روز اختلاف داشته باشد.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, last: Boolean, modifier: Modifier) {
    val colors = Atid.colors
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = colors.text,
                textAlign = TextAlign.Center,
                textDirection = TextDirection.Ltr
            ),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (last) ImeAction.Done else ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { contentDescription = label },
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .background(colors.glassWeak, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.glassLine, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) { inner() }
            }
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    val colors = Atid.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
