package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarUiState
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarViewMode
import com.bbm.applock.presentation.scheduleModule.view.component.DaysOfWeekTitle
import com.bbm.applock.presentation.scheduleModule.view.component.MonthDayComponent
import com.bbm.applock.presentation.scheduleModule.view.component.WeekDayComponent
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

@Composable
fun UsageCalendar(
    firstDataDate: LocalDate, state: CalendarUiState, onEvent: (AnalyticsEvent) -> Unit, modifier: Modifier = Modifier
) {
    val selectedDay = remember(state.selectedDateMillis) {
        Instant.ofEpochMilli(state.selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        when (state.viewMode) {
            CalendarViewMode.DAY -> WeekCalendarView(selectedDay, onEvent, firstDataDate)
            CalendarViewMode.WEEK -> MonthCalendarView(selectedDay, onEvent, firstDataDate)
            CalendarViewMode.MONTH -> MonthCalendarView(selectedDay, onEvent, firstDataDate)
        }
    }
}

@Composable
private fun MonthCalendarView(
    selectedDay: LocalDate, onEvent: (AnalyticsEvent) -> Unit, firstDataDate: LocalDate
) {
    val startMonth = YearMonth.now().minusMonths(1)
    val endMonth = YearMonth.now().plusMonths(1)

    val state = rememberCalendarState(
        startMonth = startMonth, endMonth = endMonth, firstVisibleMonth = YearMonth.now()
    )

    val coroutineScope = rememberCoroutineScope()

    HorizontalCalendar(
        state = state,
        monthHeader = { month: CalendarMonth ->
            val yearMonth = month.yearMonth
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "<",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                state.scrollToMonth(yearMonth.minusMonths(1))
                            }
                        })

                    Text(
                        text = yearMonth.month.getDisplayName(
                            java.time.format.TextStyle.FULL, Locale.getDefault()
                        ) + " " + yearMonth.year,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                state.scrollToMonth(yearMonth.plusMonths(1))
                            }
                        })
                }
                DaysOfWeekTitle(DayOfWeek.values().toList())
            }
        }, dayContent = { day: CalendarDay ->
            MonthDayComponent(
                day = day,
                cellWidth = 36.dp,
                selectedDay = selectedDay,
                currentDate = LocalDate.now(),
                firstDataDate = firstDataDate,
                shouldShowIndicator = false,
                onSelectedDayChanged = { newDay ->
                    val cal = Calendar.getInstance().apply {
                        set(newDay.year, newDay.monthValue - 1, newDay.dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onEvent(AnalyticsEvent.OnDateSelected(cal))
                })
        })
}

@Composable
private fun WeekCalendarView(
    selectedDay: LocalDate, onEvent: (AnalyticsEvent) -> Unit, firstDataDate: LocalDate
) {
    val startOfWeek = selectedDay.minusDays((selectedDay.dayOfWeek.value - 1).toLong())

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until 7) {
            val day = startOfWeek.plusDays(i.toLong())
            WeekDayComponent(
                day = WeekDay(day, WeekDayPosition.RangeDate),
                cellWidth = 47.dp,
                selectedDay = selectedDay,
                currentDate = LocalDate.now(),
                firstDataDate = firstDataDate,
                shouldShowIndicator = false,
                onSelectedDayChanged = { newDay ->
                    val cal = Calendar.getInstance().apply {
                        set(newDay.year, newDay.monthValue - 1, newDay.dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onEvent(AnalyticsEvent.OnDateSelected(cal))
                })
        }
    }
}