@file:OptIn(ExperimentalMaterial3Api::class)

package com.bbm.applock.presentation.analyticsModule.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.noRippleClickable
import java.time.format.TextStyle
import com.bbm.applock.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun UsageCalendar(
    firstDataDate: LocalDate,
    state: CalendarUiState,
    onEvent: (AnalyticsEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    val currentDate = LocalDate.now()

    var isCalendarOpen by remember { mutableStateOf(true) }
    if (!isCalendarOpen) return
    val today = LocalDate.now()

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

    val shouldShowIndicatorOnDay: (LocalDate) -> Boolean = { date ->
        !date.isBefore(firstDataDate) && !date.isAfter(today)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .wrapContentHeight()
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = currentMonthTitle.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 22.sp,
                    color = TextSecondary
                ),
            )
            Spacer(Modifier.weight(1f))

            if (selectedDay != currentDate) {
                Image(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = "Jump to current date",
                    modifier = Modifier
                        .size(24.dp)
                        .noRippleClickable {
                            onEvent(
                            AnalyticsEvent.OnDateSelected(
                                Calendar.getInstance().apply {
                                    set(today.year, today.monthValue - 1, today.dayOfMonth, 0, 0, 0)
                                }
                            ))
                        }
                )
                Spacer(Modifier.width(8.dp))
            }
            Image(
                painter = painterResource(
                    if (calendarView == CalenderViewType.MONTHLY)
                        R.drawable.ic_calender_month
                    else
                        R.drawable.ic_calender_week
                ),
                contentDescription = "Calender View Type",
                modifier = Modifier
                    .size(24.dp)
                    .noRippleClickable {
                        calendarView =
                            if (calendarView == CalenderViewType.MONTHLY)
                                CalenderViewType.WEEKLY
                            else
                                CalenderViewType.MONTHLY
                    }
            )
        }

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(calendarView == CalenderViewType.MONTHLY) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalCalendar(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    state = monthState,
                    monthHeader = { month ->
                        val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                        DaysOfWeekTitle(daysOfWeek)
                    },
                    dayContent = { day ->
                        val cellWidth = this@BoxWithConstraints.maxWidth / 7
                        MonthDayComponent(
                            day = day,
                            firstDataDate = firstDataDate,
                            cellWidth = cellWidth,
                            selectedDay = selectedDay,
                            currentDate = currentDate,
                            shouldShowIndicator = shouldShowIndicatorOnDay(day.date),
                            onSelectedDayChanged = { localDate ->
                                onEvent(
                                    AnalyticsEvent.OnDateSelected(
                                        Calendar.getInstance().apply {
                                            set(
                                                localDate.year,
                                                localDate.monthValue - 1,
                                                localDate.dayOfMonth,
                                                0, 0, 0
                                            )
                                        }
                                    )
                                )
                            },
                        )
                    }
                )
            }
        }

        AnimatedVisibility(calendarView == CalenderViewType.WEEKLY) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val cellWidth = this.maxWidth / 7
                WeekCalendar(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    state = weekState,
                    dayContent = { day ->
                        WeekDayComponent(
                            day = day,
                            cellWidth = cellWidth,
                            firstDataDate = firstDataDate,
                            selectedDay = selectedDay,
                            currentDate = currentDate,
                            shouldShowIndicator = shouldShowIndicatorOnDay(day.date),
                            onSelectedDayChanged = { localDate ->
                                onEvent(
                                    AnalyticsEvent.OnDateSelected(
                                        Calendar.getInstance().apply {
                                            set(
                                                localDate.year,
                                                localDate.monthValue - 1,
                                                localDate.dayOfMonth,
                                                0, 0, 0
                                            )
                                        }
                                    )
                                )
                            }
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
