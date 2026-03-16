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
    var visibleCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val selectedDate = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }

    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        when (viewMode) {

            CalendarViewMode.MONTH -> {
                val calendar = visibleCalendar.clone() as Calendar
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val totalCells = firstDayOfWeek + daysInMonth
                val weeks = kotlin.math.ceil(totalCells / 7.0).toInt()

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("<", fontSize = 18.sp, modifier = Modifier.clickable {
                            val newCal = (visibleCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            visibleCalendar = newCal
                            vm.setVisibleMonth(newCal)
                        })

                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(visibleCalendar.time),
                            fontSize = 16.sp,
                            color = AquaBlue
                        )

                        Text(">", fontSize = 18.sp, modifier = Modifier.clickable {
                            val newCal = (visibleCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            visibleCalendar = newCal
                            vm.setVisibleMonth(newCal)
                        })
                    }
                    Spacer(Modifier.height(8.dp))

                    for (week in 0 until weeks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (day in 0..6) {
                                val cellIndex = week * 7 + day
                                if (cellIndex >= firstDayOfWeek && cellIndex < totalCells) {
                                    val dayNumber = cellIndex - firstDayOfWeek + 1
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (dayNumber == selectedDate.get(Calendar.DAY_OF_MONTH)) AquaBlueLight else White
                                            )
                                            .clickable {
                                                val updatedDate = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                                                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                vm.onDateTapped(updatedDate)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = dayNumber.toString(), fontSize = 14.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            CalendarViewMode.DAY, CalendarViewMode.WEEK -> {
                val startOfWeek = (selectedDate.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        val dayCal = startOfWeek.clone() as Calendar
                        dayCal.add(Calendar.DAY_OF_MONTH, i)
                        val isToday = dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance()
                            .get(Calendar.DAY_OF_YEAR)
                        val today = Calendar.getInstance()
                        val isSelected =
                            dayCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                                    dayCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

                        val circleColor = if (isSelected && viewMode == AnalyticsVm.CalendarViewMode.DAY) Red else AquaBlue
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .clickable {
                                    vm.onDateTapped(dayCal)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayOfWeekFormat.format(dayCal.time).substring(0, 1),
                                    fontSize = 12.sp,
                                    color = White
                                )
                                Text(
                                    text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                    fontSize = 14.sp,
                                    color = White,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}