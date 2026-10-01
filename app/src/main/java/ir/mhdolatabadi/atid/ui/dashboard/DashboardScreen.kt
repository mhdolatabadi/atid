package ir.mhdolatabadi.atid.ui.dashboard

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.graphics.Color
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
                    targetState = current.monthLabel,
                    transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(120)) },
                    label = "monthLabel"
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
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
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val forward = targetState.monthIndex > initialState.monthIndex
                    (slideInHorizontally(tween(420)) { if (forward) it / 5 else -it / 5 } + fadeIn(tween(320))) togetherWith
                        (slideOutHorizontally(tween(260)) { if (forward) -it / 5 else it / 5 } + fadeOut(tween(160)))
                },
                label = "month"
            ) { month ->
                MonthGrid(month)
            }
        }
    }
}

@Composable
private fun MonthGrid(month: CalendarMonthUiState) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        month.days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (indexInWeek in 0 until 7) {
                    val day = week.getOrNull(indexInWeek)
                    Box(modifier = Modifier.weight(1f)) {
                        DayCell(
                            day = day,
                            isToday = day != null && day == month.todayDay,
                            isFriday = indexInWeek == FRIDAY_INDEX
                        )
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
private fun DayCell(day: Int?, isToday: Boolean, isFriday: Boolean) {
    val colors = Atid.colors
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isToday) Modifier.shadow(10.dp, CircleShape, ambientColor = colors.accent, spotColor = colors.accent) else Modifier)
                    .clip(CircleShape)
                    .background(if (isToday) colors.accent else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = PersianDateUtils.toPersianDigits(day),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isToday -> colors.onAccent
                        isFriday -> colors.holiday
                        else -> colors.text
                    }
                )
            }
        }
    }
}
