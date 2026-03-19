@file:OptIn(ExperimentalMaterial3Api::class)
package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarUiState
import com.bbm.applock.presentation.scheduleModule.view.component.DaysOfWeekTitle
import com.bbm.applock.presentation.scheduleModule.view.component.MonthDayComponent
import com.bbm.applock.presentation.scheduleModule.view.component.WeekDayComponent
import com.bbm.applock.util.CalenderViewType
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.WeekDay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun UsageCalendar(
    firstDataDate: LocalDate,
    state: CalendarUiState,
    onEvent: (AnalyticsEvent) -> Unit,
    onBack: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val currentDate = LocalDate.now()

    var isCalendarOpen by remember { mutableStateOf(true) }
    if (!isCalendarOpen) return

    val selectedDay by rememberUpdatedState(
        newValue = Instant.ofEpochMilli(state.selectedDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    )

    var calendarView by remember { mutableStateOf(CalenderViewType.MONTHLY) }

    val startMonth = YearMonth.now().minusMonths(12)
    val endMonth = YearMonth.now().plusMonths(12)

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.from(selectedDay)
    )

    val weekState = rememberWeekCalendarState(
        startDate = selectedDay.minusMonths(12),
        endDate = selectedDay.plusMonths(12),
        firstVisibleWeekDate = selectedDay
    )

    val currentMonthTitle = if (calendarView == CalenderViewType.WEEKLY) {
        weekState.firstVisibleWeek.days.first().date.month
    } else {
        monthState.firstVisibleMonth.yearMonth.month
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 0.dp)) {

        TopAppBar(windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
            title = {
                Text(
                    text = currentMonthTitle.getDisplayName(
                        java.time.format.TextStyle.FULL,
                        Locale.getDefault()
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = { isCalendarOpen = false }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    scope.launch {
                        val today = LocalDate.now()

                        if (calendarView == CalenderViewType.WEEKLY) {
                            weekState.scrollToWeek(today)
                        } else {
                            monthState.animateScrollToMonth(YearMonth.from(today))
                        }

                        onEvent(
                            AnalyticsEvent.OnDateSelected(
                                Calendar.getInstance().apply {
                                    set(today.year, today.monthValue - 1, today.dayOfMonth, 0, 0, 0)
                                }
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Today"
                    )
                }

                IconButton(onClick = {
                    calendarView =
                        if (calendarView == CalenderViewType.MONTHLY) CalenderViewType.WEEKLY
                        else CalenderViewType.MONTHLY
                }) {
                    val currentIcon =
                        if (calendarView == CalenderViewType.WEEKLY) Icons.Default.CalendarMonth
                        else Icons.Default.ViewWeek
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = "Toggle Calendar View"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
        ) {

            if (calendarView == CalenderViewType.WEEKLY) {
                WeekCalendar(
                    state = weekState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    dayContent = { day: WeekDay ->
                        val isToday = day.date == currentDate
                        val isSelected = day.date == selectedDay
                        WeekDayComponent(
                            day = day,
                            selectedDay = selectedDay,
                            currentDate = currentDate,
                            firstDataDate = firstDataDate,
                            shouldShowIndicator = true,
                            dayCircleColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            dayTextColor = when {
                                isSelected || isToday -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                        ) { newDay ->
                            onEvent(
                                AnalyticsEvent.OnDateSelected(
                                    Calendar.getInstance().apply {
                                        set(
                                            newDay.year,
                                            newDay.monthValue - 1,
                                            newDay.dayOfMonth,
                                            0, 0, 0
                                        )
                                    }
                                )
                            )
                        }
                    }
                )
            }

            if (calendarView == CalenderViewType.MONTHLY) {
                HorizontalCalendar(
                    state = monthState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    monthHeader = { month ->
                        val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                        DaysOfWeekTitle(daysOfWeek)
                    },
                    dayContent = { day: CalendarDay ->
                        val isToday = day.date == currentDate
                        val isSelected = day.date == selectedDay
                        MonthDayComponent(
                            day = day,
                            selectedDay = selectedDay,
                            currentDate = currentDate,
                            firstDataDate = firstDataDate,
                            shouldShowIndicator = true,
                            dayCircleColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            dayTextColor = when {
                                isSelected || isToday -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                        ) { newDay ->
                            onEvent(
                                AnalyticsEvent.OnDateSelected(
                                    Calendar.getInstance().apply {
                                        set(
                                            newDay.year,
                                            newDay.monthValue - 1,
                                            newDay.dayOfMonth,
                                            0, 0, 0
                                        )
                                    }
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}