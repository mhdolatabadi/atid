package ir.mhdolatabadi.atid.ui.dashboard

import androidx.compose.animation.AnimatedContent
import java.util.Date
import ir.mhdolatabadi.atid.data.Occasion
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.ui.BottomBarClearance
import ir.mhdolatabadi.atid.ui.components.glass
import ir.mhdolatabadi.atid.ui.components.pressScale
import ir.mhdolatabadi.atid.ui.theme.Atid
import ir.mhdolatabadi.atid.util.PersianDateUtils

private val weekdayLabels = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
private const val FRIDAY_INDEX = 6

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.observeAsState()
    val current = state ?: return
    val colors = Atid.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = BottomBarClearance)
    ) {
        Text(
            text = current.todayLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .glass()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Next sits first (on the right in RTL) and points left, the direction time runs in Persian.
                MonthNavButton(description = "ماه بعد", mirrored = true, onClick = { viewModel.goToNextMonth() })
                AnimatedContent(
                    targetState = current,
                    contentKey = { it.monthIndex },
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
                    label = "monthLabel",
                    modifier = Modifier.weight(1f)
                ) { month ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = month.monthLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Text(
                            text = "${month.hijriRange} • ${month.gregorianRange}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                MonthNavButton(description = "ماه قبل", mirrored = false, onClick = { viewModel.goToPreviousMonth() })
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayLabels.forEachIndexed { index, label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (index == FRIDAY_INDEX) colors.holiday else colors.muted
                        )
                    }
                }
            }

            // The next month slides in from the right, where its button is; offsets here are absolute.
            // Keyed by month, so selecting a day does not replay the slide.
            AnimatedContent(
                targetState = current,
                contentKey = { it.monthIndex },
                transitionSpec = {
                    val forward = targetState.monthIndex > initialState.monthIndex
                    (slideInHorizontally(tween(420)) { if (forward) it / 5 else -it / 5 } + fadeIn(tween(320))) togetherWith
                        (slideOutHorizontally(tween(260)) { if (forward) -it / 5 else it / 5 } + fadeOut(tween(160)))
                },
                label = "month"
            ) { month ->
                MonthGrid(month = month, selected = current.selectedDate, onSelect = viewModel::select)
            }
        }

        SelectedDayCard(current.selected)
        MonthOccasionsCard(current)
        DateConverterCard(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun MonthGrid(month: CalendarMonthUiState, selected: Date, onSelect: (Date) -> Unit) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        month.days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (indexInWeek in 0 until 7) {
                    val cell = week.getOrNull(indexInWeek)
                    Box(modifier = Modifier.weight(1f)) {
                        if (cell != null) {
                            DayCell(cell = cell, isSelected = cell.date == selected, onClick = { onSelect(cell.date) })
                        } else {
                            Box(modifier = Modifier.aspectRatio(0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavButton(description: String, mirrored: Boolean, onClick: () -> Unit) {
    val colors = Atid.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(48.dp)
            .pressScale(interaction)
            .clip(CircleShape)
            .background(colors.glassWeak)
            .border(1.dp, colors.glassEdge, CircleShape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron),
            contentDescription = description,
            tint = colors.text,
            modifier = Modifier.graphicsLayer { scaleX = if (mirrored) -1f else 1f }
        )
    }
}

@Composable
private fun DayCell(cell: DayCellState, isSelected: Boolean, onClick: () -> Unit) {
    val colors = Atid.colors
    val shape = RoundedCornerShape(10.dp)
    val interaction = remember { MutableInteractionSource() }
    val background = when {
        cell.isToday -> colors.accent
        cell.isHoliday -> colors.holidaySoft
        else -> colors.glassWeak
    }
    val main = when {
        cell.isToday -> colors.onAccent
        cell.isHoliday -> colors.holiday
        else -> colors.text
    }
    val sub = if (cell.isToday) colors.onAccent else colors.muted
    Column(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .pressScale(interaction, pressed = 0.94f)
            .then(if (cell.isToday) Modifier.shadow(8.dp, shape, ambientColor = colors.accent, spotColor = colors.accent) else Modifier)
            .clip(shape)
            .background(background)
            .then(if (isSelected && !cell.isToday) Modifier.border(1.5.dp, colors.accent, shape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = PersianDateUtils.toPersianDigits(cell.solarDay),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = main
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = PersianDateUtils.toArabicDigits(cell.hijriDay), fontSize = 10.sp, color = sub)
            Text(text = cell.gregorianDay.toString(), fontSize = 10.sp, color = sub)
        }
    }
}

@Composable
private fun SelectedDayCard(details: DayDetails) {
    val colors = Atid.colors
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
    ) {
        AnimatedContent(
            targetState = details,
            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
            label = "selectedDay"
        ) { day ->
            Column {
                Text(text = day.solarLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.text)
                Text(
                    text = "${day.hijriLabel} • ${day.gregorianLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                if (day.occasions.isEmpty()) {
                    Text(text = "مناسبتی ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                } else {
                    day.occasions.forEach { OccasionLine(dayLabel = null, occasion = it) }
                }
            }
        }
    }
}

@Composable
private fun MonthOccasionsCard(month: CalendarMonthUiState) {
    val colors = Atid.colors
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
    ) {
        Text(
            text = "مناسبت‌های ${month.monthLabel}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (month.monthOccasions.isEmpty()) {
            Text(text = "مناسبتی ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
        } else {
            month.monthOccasions.forEach { OccasionLine(dayLabel = it.dayLabel, occasion = it.occasion) }
        }
        Text(
            text = "تاریخ‌های قمری محاسبه‌ای‌اند و ممکن است با تقویم رسمی کشور یک روز اختلاف داشته باشند.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun OccasionLine(dayLabel: String?, occasion: Occasion) {
    val colors = Atid.colors
    val color = if (occasion.holiday) colors.holiday else colors.text
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dayLabel != null) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (occasion.holiday) colors.holiday else colors.muted,
                modifier = Modifier.width(76.dp)
            )
        }
        Text(text = occasion.title, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.weight(1f))
        if (occasion.holiday) {
            Text(
                text = "تعطیل",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.holiday,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(colors.holidaySoft, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
