package com.bbm.applock.presentation.scheduleModule.view.component

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.White
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.noRippleClickable
import com.kizitonwose.calendar.core.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun MonthDayComponent(
    day: CalendarDay,
    selectedDay: LocalDate?,
    currentDate: LocalDate?,
    firstDataDate: LocalDate,
    cellWidth: Dp,
    shouldShowIndicator: Boolean = true,
    onSelectedDayChanged: (LocalDate) -> Unit
) {

    val isFutureDate = currentDate?.let { day.date.isAfter(it) } ?: false
    val isSelectable = !isFutureDate && !day.date.isBefore(firstDataDate)
    val context = LocalContext.current
    val isToday = currentDate != null && day.date == currentDate
    val isSelected = selectedDay != null && selectedDay == day.date
    val tappedAgain = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(cellWidth)
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(when {
                tappedAgain.value -> AquaBlue
                isSelected -> Red
                else -> AquaBlue
            })
            .border(
                border = BorderStroke(
                    if (isToday) 1.3.dp else 0.dp,
                    color = AquaBlueBorder
                ), shape = CircleShape
            )
            .noRippleClickable {
                if (!isSelectable) {
                    Toast.makeText(context, "No data available for this date", Toast.LENGTH_SHORT).show()
                } else {
                    if (isSelected) {
                        tappedAgain.value = !tappedAgain.value
                        if (tappedAgain.value) {
                            onSelectedDayChanged(day.date) // fetch weekly data
                        }
                    } else {
                        tappedAgain.value = false
                        onSelectedDayChanged(day.date) // fetch daily data
                    }
                }
            }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val color = WhiteColor
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = White
            ),
            textAlign = TextAlign.Center,
        )

        if (shouldShowIndicator)
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(White)
            )
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                text = dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
