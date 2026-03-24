package com.bbm.applock.presentation.scheduleModule.view.component

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.White
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
    val isSelected = selectedDay == day.date

    Column(
        modifier = Modifier
            .width(cellWidth)
            .padding(horizontal = 4.dp)
            .noRippleClickable {
                if (isSelectable) {
                    onSelectedDayChanged(day.date)
                } else {
                    Toast.makeText(context, "No data available for this date", Toast.LENGTH_SHORT)
                        .show()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W500,
                fontSize = 14.sp,
                color = if (isSelected) AquaBlueBorder else TextPrimary
            ),
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) Red else AquaBlue)
                .border(
                    border = BorderStroke(
                        if (currentDate != day.date) 0.dp else 1.3.dp, color = AquaBlueBorder
                    ), shape = CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dateFormatter.format(day.date),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(0.5.dp))
                if (shouldShowIndicator) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(color = White)
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }


    }
}