package com.bbm.applock.presentation.scheduleModule.view.component

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.util.dateFormatter
import com.bbm.applock.util.noRippleClickable
import com.kizitonwose.calendar.core.WeekDay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekDayComponent(
    day: WeekDay,
    cellWidth: Dp,
    firstDataDate: LocalDate,
    selectedDay: LocalDate,
    currentDate: LocalDate,
    shouldShowIndicator: Boolean = false,
    onSelectedDayChanged: (LocalDate) -> Unit
) {

    val context = LocalContext.current

    val isFutureDate = day.date.isAfter(currentDate)
    val isSelectable = !isFutureDate && !day.date.isBefore(firstDataDate)

    Column(
        modifier = Modifier
            .width(cellWidth)
            .padding(horizontal = 4.dp)
            .noRippleClickable {
                if (isSelectable) {
                    onSelectedDayChanged(day.date)
                } else {
                    Toast.makeText(context, "No data available for this date", Toast.LENGTH_SHORT).show()
                }
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selectedDay == day.date) AquaBlueBorder
                else LightBlue
            )
            .border(
                border = BorderStroke(
                    if (currentDate != day.date) (-1).dp else 1.3.dp, color = AquaBlueBorder
                ), shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween) {
        val color = if (selectedDay == day.date) Color.White else TextPrimary
        Text(
            text = day.date.dayOfWeek.getDisplayName(
                TextStyle.SHORT, Locale.getDefault()
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400, fontSize = 14.sp, color = color
            ),
            textAlign = TextAlign.Center,
        )

        Text(
            text = dateFormatter.format(day.date),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400, fontSize = 14.sp, color = color
            ),
            textAlign = TextAlign.Center,
        )

        if (shouldShowIndicator) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(color = color)
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}