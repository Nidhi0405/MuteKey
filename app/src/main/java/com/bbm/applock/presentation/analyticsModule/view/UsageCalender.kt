package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm.CalendarViewMode
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.White
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UsageCalendar(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier
) {
    val viewMode by vm.viewMode.collectAsState()
    val selectedDateMillis by vm.selectedDateMillis.collectAsState()

    val selectedDate = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(12.dp)
    ) {
        when (viewMode) {
            CalendarViewMode.MONTH -> MonthView(selectedDate, vm)
            CalendarViewMode.DAY -> WeekView(selectedDate, vm)
            CalendarViewMode.WEEK -> MonthView(selectedDate, vm)
        }
    }
}

@Composable
private fun MonthView(
    selectedDate: Calendar,
    vm: AnalyticsVm
) {
    val visibleMonth by vm.visibleMonth.collectAsState()
    val calendar = visibleMonth.clone() as Calendar

    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = firstDayOffset + daysInMonth
    val weeks = (totalCells + 6) / 7

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("<", modifier = Modifier.clickable {
                vm.setVisibleMonth((visibleMonth.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                })
            })

            Text(
                SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(visibleMonth.time),
                color = AquaBlue
            )

            Text(">", modifier = Modifier.clickable {
                vm.setVisibleMonth((visibleMonth.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                })
            })
        }

        Spacer(Modifier.height(8.dp))

        repeat(weeks) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { day ->
                    val index = week * 7 + day

                    if (index >= firstDayOffset && index < totalCells) {
                        val dayNumber = index - firstDayOffset + 1

                        val isSelected =
                            selectedDate.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                                    selectedDate.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH) &&
                                    selectedDate.get(Calendar.YEAR) == visibleMonth.get(Calendar.YEAR)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AquaBlueLight else White)
                                .clickable {
                                    val date = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, visibleMonth.get(Calendar.YEAR))
                                        set(Calendar.MONTH, visibleMonth.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    vm.onDateTapped(date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayNumber.toString())
                        }
                    } else {
                        Spacer(Modifier.size(36.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun WeekView(
    selectedDate: Calendar,
    vm: AnalyticsVm
) {
    val startOfWeek = (selectedDate.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(7) { i ->
            val day = startOfWeek.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, i)

            val isSelected =
                day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                        day.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Red else AquaBlue)
                    .clickable {
                        vm.onDateTapped(day)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayFormat.format(day.time).take(1),
                        fontSize = 12.sp,
                        color = White
                    )
                    Text(
                        text = day.get(Calendar.DAY_OF_MONTH).toString(),
                        fontWeight = Bold,
                        color = White
                    )
                }
            }
        }
    }
}