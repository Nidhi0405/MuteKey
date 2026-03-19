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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.noRippleClickable
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun MonthDayComponent(
    day: CalendarDay,
    cellWidth: Dp,
    selectedDay: LocalDate,
    currentDate: LocalDate,
    firstDataDate: LocalDate,
    shouldShowIndicator: Boolean = false,
    onSelectedDayChanged: (LocalDate) -> Unit
) {

    val isCurrentMonth = day.position == DayPosition.MonthDate
    val isFutureDate = day.date.isAfter(currentDate)
    val isSelected = selectedDay == day.date && !isFutureDate
    val isSelectable = !isFutureDate && !day.date.isBefore(firstDataDate)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(cellWidth)
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selectedDay == day.date) AquaBlueBorder else LightBlue)
            .border(
                border = BorderStroke(
                    if (currentDate != day.date) (-1).dp else 1.3.dp,
                    color = AquaBlueBorder
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .noRippleClickable {
                if (isSelectable) {
                    onSelectedDayChanged(day.date)
                } else {
                    Toast.makeText(context, "No data available for this date", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val backgroundColor = when {
            isSelected -> AquaBlueBorder
            else -> LightBlue
        }

        val textColor = when {
            isSelected -> WhiteColor
            isCurrentMonth -> TextPrimary
            else -> TextPrimary.copy(alpha = 0.3f)
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = textColor
            ),
            textAlign = TextAlign.Center,
        )

        if (shouldShowIndicator)
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
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