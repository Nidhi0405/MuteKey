package com.bbm.applock.presentation.scheduleModule.view.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.util.noRippleClickable
import com.kizitonwose.calendar.core.WeekDay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekDayComponent(
    day: WeekDay,
    selectedDay: LocalDate,
    currentDate: LocalDate,
    firstDataDate: LocalDate,
    shouldShowIndicator: Boolean = true,
    dayCircleColor: Color = Color.Transparent,
    dayTextColor: Color = Color.Black,
    onSelectedDayChanged: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cellWidth = screenWidth / 7

    val isFutureDate = day.date.isAfter(currentDate)
    val isSelectable = !isFutureDate && !day.date.isBefore(firstDataDate)
    val isSelected = selectedDay == day.date && !isFutureDate
    val isToday = day.date == currentDate

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val indicatorColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .width(cellWidth)
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .noRippleClickable {
                if (isSelectable) {
                    onSelectedDayChanged(day.date)
                } else {
                    Toast.makeText(context, "No data available for this date", Toast.LENGTH_SHORT).show()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (shouldShowIndicator) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            } else {
                Box(modifier = Modifier.size(4.dp))
            }
        }
    }
}